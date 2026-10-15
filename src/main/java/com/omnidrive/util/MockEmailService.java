package com.omnidrive.util;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementación en memoria del servicio de correo para pruebas, auditoría y desarrollo local sin dependencias SMTP.
 */
public class MockEmailService implements EmailService, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public record SentEmail(String to, String subject, String body, Instant sentAt) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private final List<SentEmail> sentEmails = new CopyOnWriteArrayList<>();

    @Override
    public void sendEmail(String to, String subject, String body) {
        sentEmails.add(new SentEmail(to, subject, body, Instant.now()));
    }

    /**
     * Retorna una lista inmutable con los correos enviados para aserciones de prueba.
     */
    public List<SentEmail> getSentEmails() {
        return Collections.unmodifiableList(sentEmails);
    }

    /**
     * Limpia la bandeja de salida en memoria.
     */
    public void clear() {
        sentEmails.clear();
    }
}
