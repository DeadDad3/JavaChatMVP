package ru.kudukhov;

import ru.kudukhov.network.ClientSession;
import ru.kudukhov.ui.LoginWindow;

public class Main {
  public static void main(String[] args) {
    try {
      // Создаем сессию клиента для подключения к серверу на localhost и порту 10001
      ClientSession clientSession = new ClientSession("localhost", 10001);

      // Открываем окно для входа
      LoginWindow loginWindow = new LoginWindow(clientSession);
      loginWindow.setVisible(true);

      // Запускаем клиентскую сессию
      clientSession.start();

    } catch (Exception e) {
      System.err.println("Ошибка запуска клиента: " + e.getMessage());
      e.printStackTrace();
    }
  }
}