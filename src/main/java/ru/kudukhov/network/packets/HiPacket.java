package ru.kudukhov.network.packets;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class HiPacket extends Packet {
    public static final String type = "HI";

    public String login;
    public String password; // Новое поле для пароля

    public String getType() {
        return type;
    }

    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(login);
        writer.println(password); // Передаем пароль
    }

    public void readBody(BufferedReader reader) throws Exception {
        login = reader.readLine();
        password = reader.readLine(); // Читаем пароль
    }
}