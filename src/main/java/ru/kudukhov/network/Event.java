package ru.kudukhov.network;

import ru.kudukhov.network.packets.Packet;

public class Event {
    public Session session;
    public Packet packet;

    public Event(Session session, Packet packet) {
        this.session = session;
        this.packet = packet;
    }
}