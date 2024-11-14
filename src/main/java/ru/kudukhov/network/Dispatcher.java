package ru.kudukhov.network;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import ru.kudukhov.model.Correspondent;
import ru.kudukhov.network.packets.*;

public class Dispatcher implements Runnable {
    private static final LinkedBlockingQueue<Event> packetQueue = new LinkedBlockingQueue<>();

    // Предопределённые логины и пароли для авторизации
    private static final Map<String, String> userCredentials = Map.of(
        "user1", "pass1",
        "user2", "pass2",
        "user3", "pass3"
    );

    public static void event(Event e) {
        packetQueue.add(e);
    }

    public void run() {
        for (;;) {
            try {
                var e = packetQueue.take();
                processPacket(e.session, e.packet);
            } catch (InterruptedException x) {
                break;
            }
        }
    }

    private void processPacket(Session session, Packet p) {
        System.out.println("Processing packet: " + p.getType());
        try {
            switch (p) {
                case EchoPacket echoP -> {
                    session.send(p);
                }

                case HiPacket hiP -> {
                    // Проверка логина и пароля при авторизации
                    var correctPassword = userCredentials.get(hiP.login);
                    if (correctPassword != null && correctPassword.equals(hiP.password)) {
                        var correspondent = Correspondent.findCorrespondent(hiP.login);
                        if (correspondent == null) {
                            session.close();
                            return;
                        }
                        session.correspondent = correspondent;
                        correspondent.activeSession = session;
                        System.out.println("ru.kudukhov.model.Correspondent authorized, id: " + correspondent.id);
                    } else {
                        System.out.println("Authorization failed: incorrect password");
                        session.close();
                    }
                }

                case MessagePacket mP -> {
                    if (session.correspondent == null) {
                        System.out.println("Non-authorized");
                        return;
                    }

                    var correspondent = Correspondent.findCorrespondent(mP.correspondentId);
                    mP.correspondentId = session.correspondent.id;
                    mP.senderName = session.correspondent.login; // Устанавливаем имя отправителя

                    if (correspondent != null && correspondent.activeSession != null) {
                        System.out.println("Sending message to correspondent, id: " + correspondent.id);
                        correspondent.activeSession.send(mP);
                    } else {
                        System.out.println("Target correspondent not connected, id: " + correspondent.id);
                    }
                }

                case ListPacket emptyListP -> {
                    var filledListP = new ListPacket();
                    for (var c : Correspondent.listAll()) {
                        filledListP.addItem(c.id, c.login);
                    }
                    session.send(filledListP);
                }

                default -> {
                    System.out.println("Unexpected packet type: " + p.getType());
                }
            }
        } catch (Exception ex) {
            System.out.println("ru.kudukhov.network.Dispatcher problem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}