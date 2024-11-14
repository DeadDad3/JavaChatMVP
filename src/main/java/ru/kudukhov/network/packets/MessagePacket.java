package ru.kudukhov.network.packets;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class MessagePacket extends Packet {
    public static final String type = "MSG";

    public int correspondentId;
    public String text;
    public String senderName; // Новое поле для имени отправителя

    public String getType() {
        return type;
    }

    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(correspondentId);
        writer.println(senderName); // Записываем имя отправителя
        writer.println(text);
        writer.println();
    }

    public void readBody(BufferedReader reader) throws Exception {
        var correspondentIdText = reader.readLine();
        correspondentId = Integer.parseInt(correspondentIdText);

        senderName = reader.readLine(); // Читаем имя отправителя
        text = readText(reader);
    }
}