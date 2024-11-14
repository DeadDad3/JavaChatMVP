package ru.kudukhov.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.kudukhov.network.ClientSession;
import ru.kudukhov.network.packets.ListPacket.CorrespondentItem;
import ru.kudukhov.network.packets.MessagePacket;

public class MainWindow extends JFrame {
  private JTextArea chatArea;
  private JTextField messageField;
  private JButton sendButton;
  private JList<String> userList; // Панель для отображения списка пользователей
  private DefaultListModel<String> userListModel; // Модель данных для списка пользователей
  private ClientSession clientSession;
  private String username;

  // Локальное хранение ID пользователей и логинов
  private Map<String, Integer> userIdMap = new HashMap<>();
  private String selectedUser; // Выбранный пользователь для отправки сообщения

  public MainWindow(ClientSession clientSession, String username) {
    this.clientSession = clientSession;
    this.username = username;
    setTitle("Чат - " + username);
    setSize(600, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    initUI();
  }

  private void initUI() {
    // Основное окно чата
    chatArea = new JTextArea();
    chatArea.setEditable(false);
    chatArea.setLineWrap(true);

    JScrollPane scrollPane = new JScrollPane(chatArea);

    // Поле для ввода сообщений
    messageField = new JTextField();
    sendButton = new JButton("Отправить");

    JPanel inputPanel = new JPanel(new BorderLayout());
    inputPanel.add(messageField, BorderLayout.CENTER);
    inputPanel.add(sendButton, BorderLayout.EAST);

    // Боковая панель для списка пользователей
    userListModel = new DefaultListModel<>();
    userList = new JList<>(userListModel);
    JScrollPane userScrollPane = new JScrollPane(userList);

    // Добавляем обработчик выбора пользователя в списке
    userList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        selectedUser = userList.getSelectedValue(); // Устанавливаем выбранного пользователя
      }
    });

    // Основное расположение компонентов
    add(scrollPane, BorderLayout.CENTER);
    add(inputPanel, BorderLayout.SOUTH);
    add(userScrollPane, BorderLayout.EAST);

    // Обработчик кнопки отправки
    sendButton.addActionListener(e -> sendMessage());
  }

  private void sendMessage() {
    String message = messageField.getText().trim();
    if (!message.isEmpty() && selectedUser != null) {
      try {
        // Получаем ID выбранного пользователя для отправки сообщения
        Integer correspondentId = userIdMap.get(selectedUser);

        if (correspondentId != null) {
          // Создаём пакет сообщения и задаём текст и ID корреспондента
          MessagePacket messagePacket = new MessagePacket();
          messagePacket.text = message;
          messagePacket.correspondentId = correspondentId;

          // Отправляем пакет через клиентскую сессию
          clientSession.sendPacket(messagePacket);

          // Отображаем отправленное сообщение в окне чата
          chatArea.append(username + ": " + message + "\n");
          messageField.setText("");
        } else {
          JOptionPane.showMessageDialog(this, "Не удалось найти ID пользователя.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Ошибка при отправке сообщения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(this, "Выберите пользователя для отправки сообщения.", "Ошибка", JOptionPane.WARNING_MESSAGE);
    }
  }

  // Метод для обновления списка пользователей
  public void updateUserList(List<CorrespondentItem> users) {
    userListModel.clear();
    userIdMap.clear();

    // Добавляем каждого пользователя в локальный список и сохраняем ID
    for (CorrespondentItem user : users) {
      userListModel.addElement(user.login);
      userIdMap.put(user.login, user.id); // Сохраняем соответствие логина и ID
    }
  }

  // Метод для отображения сообщений от других пользователей
  public void receiveMessage(String sender, String message) {
    chatArea.append(sender + ": " + message + "\n");
  }
}