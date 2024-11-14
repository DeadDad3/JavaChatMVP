package ru.kudukhov.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import ru.kudukhov.network.ClientSession;
import ru.kudukhov.network.packets.ListPacket.CorrespondentItem;
import ru.kudukhov.network.packets.MessagePacket;

public class MainWindow extends JFrame {
  private JPanel chatPanel;
  private JScrollPane chatScrollPane;
  private JTextField messageField;
  private JButton sendButton;
  private JList<String> userList;
  private DefaultListModel<String> userListModel;
  private ClientSession clientSession;
  private String username;

  private Map<String, Integer> userIdMap = new HashMap<>();
  private Map<String, List<String>> chatHistories = new HashMap<>();
  private String selectedUser;

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
    chatPanel = new JPanel();
    chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
    chatPanel.setBackground(new Color(25, 34, 49));
    chatPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

    chatScrollPane = new JScrollPane(chatPanel);
    chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    chatScrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));

    messageField = new JTextField();
    sendButton = new JButton("Отправить");

    JPanel inputPanel = new JPanel(new BorderLayout());
    inputPanel.add(messageField, BorderLayout.CENTER);
    inputPanel.add(sendButton, BorderLayout.EAST);

    userListModel = new DefaultListModel<>();
    userList = new JList<>(userListModel);
    userList.setFixedCellHeight(50);
    userList.setFont(new Font("Arial", Font.PLAIN, 16));
    JScrollPane userScrollPane = new JScrollPane(userList);
    userScrollPane.setBorder(new EmptyBorder(0, 5, 0, 5));

    add(chatScrollPane, BorderLayout.CENTER);
    add(inputPanel, BorderLayout.SOUTH);
    add(userScrollPane, BorderLayout.WEST);

    sendButton.addActionListener(e -> sendMessage());

    messageField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }
      }
    });

    userList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        selectedUser = userList.getSelectedValue();
        displayChatHistory(selectedUser);
      }
    });
  }

  private void sendMessage() {
    String message = messageField.getText().trim();
    if (!message.isEmpty() && selectedUser != null) {
      try {
        Integer correspondentId = userIdMap.get(selectedUser);

        if (correspondentId != null) {
          MessagePacket messagePacket = new MessagePacket();
          messagePacket.text = message;
          messagePacket.correspondentId = correspondentId;

          clientSession.sendPacket(messagePacket);

          if (!selectedUser.equals(username)) {
            addMessageBubble(username, message, true);
            chatHistories.computeIfAbsent(selectedUser, k -> new ArrayList<>()).add("Вы: " + message);
          }
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

  private void displayChatHistory(String user) {
    chatPanel.removeAll();
    List<String> history = chatHistories.getOrDefault(user, new ArrayList<>());
    for (String message : history) {
      boolean isUserMessage = message.startsWith("Вы:");
      addMessageBubble(isUserMessage ? username : user, message.replaceFirst("Вы: ", ""), isUserMessage);
    }
    chatPanel.revalidate();
    chatPanel.repaint();
  }

  private void addMessageBubble(String sender, String message, boolean isUserMessage) {
    JPanel bubblePanel = new JPanel();
    bubblePanel.setLayout(new FlowLayout(isUserMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
    bubblePanel.setBackground(new Color(25, 34, 49));

    JTextArea messageBubble = new JTextArea(message);
    messageBubble.setEditable(false);
    messageBubble.setLineWrap(true);
    messageBubble.setWrapStyleWord(true);
    messageBubble.setOpaque(true);
    messageBubble.setBorder(new EmptyBorder(8, 12, 8, 12));
    messageBubble.setBackground(isUserMessage ? new Color(58, 122, 187) : new Color(47, 54, 64));
    messageBubble.setForeground(Color.WHITE);
    messageBubble.setFont(new Font("Arial", Font.PLAIN, 14));
    messageBubble.setMaximumSize(new Dimension(chatScrollPane.getWidth() * 6 / 10, Integer.MAX_VALUE)); // Ограничение по ширине

    bubblePanel.add(messageBubble);
    chatPanel.add(bubblePanel);

    chatPanel.revalidate();
    chatScrollPane.getVerticalScrollBar().setValue(chatScrollPane.getVerticalScrollBar().getMaximum());
  }

  public void updateUserList(List<CorrespondentItem> users) {
    userListModel.clear();
    userIdMap.clear();

    for (CorrespondentItem user : users) {
      userListModel.addElement(user.login);
      userIdMap.put(user.login, user.id);
    }
  }

  public void receiveMessage(String sender, String message) {
    chatHistories.computeIfAbsent(sender, k -> new ArrayList<>()).add(sender + ": " + message);

    if (sender.equals(selectedUser)) {
      addMessageBubble(sender, message, false);
    }
  }
}