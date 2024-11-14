package ru.kudukhov.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class ThemeManager {

  public static void applyDarkBlueTheme() {
    // Устанавливаем тему FlatDarkLaf
    FlatDarkLaf.setup();

    // Настройка основных цветов
    UIManager.put("Panel.background", new Color(25, 34, 49));          // Фон панели
    UIManager.put("Button.background", new Color(44, 62, 80));         // Цвет кнопок
    UIManager.put("Button.foreground", Color.WHITE);                   // Цвет текста на кнопках
    UIManager.put("TextField.background", new Color(33, 47, 61));      // Фон текстовых полей
    UIManager.put("TextField.foreground", Color.WHITE);                // Цвет текста в текстовых полях
    UIManager.put("TextArea.background", new Color(25, 34, 49));       // Фон текстовой области (чат)
    UIManager.put("TextArea.foreground", Color.WHITE);                 // Цвет текста в текстовой области
    UIManager.put("List.background", new Color(25, 34, 49));           // Фон списка пользователей
    UIManager.put("List.foreground", Color.WHITE);                     // Цвет текста в списке пользователей
    UIManager.put("ScrollPane.background", new Color(25, 34, 49));     // Фон для прокручиваемых панелей
    UIManager.put("Label.foreground", Color.WHITE);                    // Цвет текста для меток
    UIManager.put("OptionPane.background", new Color(25, 34, 49));     // Фон сообщений
    UIManager.put("OptionPane.messageForeground", Color.WHITE);        // Цвет текста сообщений

    // Применяем тему ко всем компонентам Swing
    try {
      UIManager.setLookAndFeel(new FlatDarkLaf());
    } catch (UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }
  }
}