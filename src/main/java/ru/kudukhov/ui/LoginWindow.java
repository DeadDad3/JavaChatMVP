package ru.kudukhov.ui;

import javax.swing.*;
import java.awt.*;
import ru.kudukhov.network.ClientSession;
import ru.kudukhov.network.packets.HiPacket;
import ru.kudukhov.network.packets.ListPacket;

public class LoginWindow extends JFrame {
  private JTextField loginField;
  private JPasswordField passwordField; // Поле для пароля
  private JButton loginButton;
  private ClientSession clientSession;

  public LoginWindow(ClientSession clientSession) {
    this.clientSession = clientSession;
    setTitle("Авторизация");
    setSize(350, 200); // Увеличиваем размер окна
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    initUI();
  }

  private void initUI() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(3, 2, 10, 10)); // Сетка 3x2 с отступами

    loginField = new JTextField();
    passwordField = new JPasswordField(); // Поле для пароля
    loginButton = new JButton("Войти");

    panel.add(new JLabel("Введите логин:"));
    panel.add(loginField);
    panel.add(new JLabel("Введите пароль:"));
    panel.add(passwordField);
    panel.add(new JLabel("")); // Пустое пространство
    panel.add(loginButton);

    add(panel, BorderLayout.CENTER);

    // Обработчик события кнопки "Войти"
    loginButton.addActionListener(e -> {
      String login = loginField.getText().trim();
      String password = new String(passwordField.getPassword()).trim(); // Получаем пароль

      if (!login.isEmpty() && !password.isEmpty()) {
        authorize(login, password);
      } else {
        JOptionPane.showMessageDialog(this, "Введите логин и пароль", "Ошибка", JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  private void authorize(String login, String password) {
    try {
      HiPacket hiPacket = new HiPacket();
      hiPacket.login = login;
      hiPacket.password = password;

      clientSession.sendPacket(hiPacket);

      // Проверяем, не закрылось ли соединение из-за неверного пароля
      if (clientSession.isSessionClosed()) {
        JOptionPane.showMessageDialog(this, "Ошибка авторизации: неправильный логин или пароль", "Ошибка", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Открытие основного окна чата
      MainWindow mainWindow = new MainWindow(clientSession, login);
      clientSession.setMainWindow(mainWindow);
      mainWindow.setVisible(true);

      // Запрашиваем список пользователей после авторизации
      ListPacket listPacket = new ListPacket();
      clientSession.sendPacket(listPacket);

      dispose();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Ошибка при авторизации: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
  }
}