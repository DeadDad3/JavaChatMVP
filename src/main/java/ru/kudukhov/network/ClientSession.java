package ru.kudukhov.network;

import javax.swing.JOptionPane;
import ru.kudukhov.network.packets.*;
import ru.kudukhov.ui.MainWindow;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientSession extends Thread {
  private final Socket socket;
  private BufferedReader reader;
  private PrintWriter writer;
  private final LinkedBlockingQueue<Packet> outgoingQueue = new LinkedBlockingQueue<>();
  private MainWindow mainWindow; // Ссылка на главное окно чата

  // Конструктор, устанавливающий соединение с сервером
  public ClientSession(String host, int port) throws IOException {
    // Устанавливаем подключение к серверу с указанным хостом и портом
    this.socket = new Socket(host, port);

    // Инициализация потоков для чтения и записи данных через сокет
    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    writer = new PrintWriter(socket.getOutputStream(), true);
  }

  // Установка ссылки на главное окно чата для обновления UI
  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  // Метод для отправки пакета на сервер
  public void sendPacket(Packet packet) {
    outgoingQueue.add(packet);
  }

  @Override
  public void run() {
    // Поток для отправки пакетов из очереди на сервер
    new Thread(() -> {
      try {
        while (!socket.isClosed()) {
          Packet packet = outgoingQueue.take();
          packet.writePacket(writer);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }).start();

    // Основной поток для получения пакетов от сервера
    try {
      while (!socket.isClosed()) {
        Packet packet = Packet.readPacket(reader);
        if (packet == null) {
          // Сервер закрыл соединение, завершаем сессию
          System.out.println("Сервер завершил соединение.");
          close();
          JOptionPane.showMessageDialog(null, "Ошибка авторизации: неправильный логин или пароль", "Ошибка", JOptionPane.ERROR_MESSAGE);
          System.exit(0); // Завершение программы при неудачной авторизации
          return;
        }

        // Обработка полученного пакета
        handlePacket(packet);
      }
    } finally {
      close();
    }
  }

  // Метод для обработки различных типов пакетов
  private void handlePacket(Packet packet) {
    if (packet instanceof MessagePacket) {
      MessagePacket messagePacket = (MessagePacket) packet;
      if (mainWindow != null) {
        mainWindow.receiveMessage(messagePacket.senderName, messagePacket.text);
      }
    } else if (packet instanceof ListPacket) {
      ListPacket listPacket = (ListPacket) packet;
      if (mainWindow != null) {
        mainWindow.updateUserList(listPacket.items);
      }
    } else {
      System.out.println("Получен пакет неизвестного типа: " + packet.getType());
    }
  }

  public boolean isSessionClosed() {
    return socket.isClosed();
  }

  // Метод для закрытия сокета и завершения сессии
  public void close() {
    try {
      socket.close();
    } catch (IOException e) {
      System.err.println("Ошибка при закрытии клиентской сессии: " + e.getMessage());
    }
  }
}