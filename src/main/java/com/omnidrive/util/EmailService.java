package com.omnidrive.util;

/**
 * Contrato para el servicio de envío y notificación de correos electrónicos en OmniDrive.
 */
public interface EmailService {

    /**
     * Envía un mensaje de correo electrónico al destinatario indicado.
     *
     * @param to Dirección de correo de destino.
     * @param subject Asunto del mensaje.
     * @param body Contenido del mensaje.
     */
    void sendEmail(String to, String subject, String body);
}
