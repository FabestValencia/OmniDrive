package com.omnidrive.model;

import com.omnidrive.util.EmailService;
import com.omnidrive.util.MockEmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Tokens de Recuperación y Servicio de Correo (T4)")
class PasswordRecoveryTokenTest {

    @Test
    @DisplayName("El token debe ser válido dentro de la ventana de 15 minutos")
    void testTokenValidWithin15Minutes() {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        PasswordRecoveryToken token = new PasswordRecoveryToken("carlos.vendedor", "SEC123", now);

        assertEquals("carlos.vendedor", token.getUsername());
        assertEquals("SEC123", token.getCode());
        assertFalse(token.isUsed());
        assertEquals(now.plus(Duration.ofMinutes(15)), token.getExpiresAt());

        // A los 10 minutos (dentro de ventana)
        Instant at10Minutes = now.plus(Duration.ofMinutes(10));
        assertTrue(token.isValid("SEC123", at10Minutes));
    }

    @Test
    @DisplayName("El token debe ser inválido después de 15 minutos (expiración)")
    void testTokenExpiredAfter15Minutes() {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        PasswordRecoveryToken token = new PasswordRecoveryToken("carlos.vendedor", "SEC123", now);

        // A los 15 minutos y 1 segundo (expirado)
        Instant at15MinutesAndOneSecond = now.plus(Duration.ofMinutes(15)).plusSeconds(1);
        assertFalse(token.isValid("SEC123", at15MinutesAndOneSecond));
    }

    @Test
    @DisplayName("El token debe rechazar códigos incorrectos")
    void testTokenRejectsWrongCode() {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        PasswordRecoveryToken token = new PasswordRecoveryToken("carlos.vendedor", "SEC123", now);

        assertFalse(token.isValid("WRONG_CODE", now.plus(Duration.ofMinutes(2))));
        assertFalse(token.isValid(null, now.plus(Duration.ofMinutes(2))));
    }

    @Test
    @DisplayName("El token no debe ser válido después de ser utilizado (uso único)")
    void testTokenSingleUse() {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        PasswordRecoveryToken token = new PasswordRecoveryToken("carlos.vendedor", "SEC123", now);

        assertTrue(token.isValid("SEC123", now.plus(Duration.ofMinutes(1))));

        token.markAsUsed();
        assertTrue(token.isUsed());

        assertFalse(token.isValid("SEC123", now.plus(Duration.ofMinutes(2))));
    }

    @Test
    @DisplayName("MockEmailService debe registrar los correos enviados en memoria")
    void testMockEmailService() {
        MockEmailService emailService = new MockEmailService();
        assertEquals(0, emailService.getSentEmails().size());

        emailService.sendEmail("carlos@example.com", "Código de Recuperación", "Tu código es: SEC123");

        assertEquals(1, emailService.getSentEmails().size());
        MockEmailService.SentEmail sent = emailService.getSentEmails().get(0);
        assertEquals("carlos@example.com", sent.to());
        assertEquals("Código de Recuperación", sent.subject());
        assertTrue(sent.body().contains("SEC123"));

        emailService.clear();
        assertEquals(0, emailService.getSentEmails().size());
    }
}
