package com.is1.proyecto.services;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
    
    // Tus credenciales
    private static final String REMITENTE = "holaqhaceonce@gmail.com"; 
    private static final String PASSWORD_APP = "moiytnvscmoewmvz";

    public static void enviarCorreoConfirmacion(String id, String correo_destinatario, String token) {
        
        // 1. Configuramos las propiedades del servidor SMTP de Gmail
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Encriptación TLS obligatoria

        // 2. Iniciamos la sesión con nuestras credenciales
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD_APP);
            }
        });

        try {
            // 3. Armamos el mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correo_destinatario));
            
            // Asunto del correo
            message.setSubject("Confirmación de cuenta y creación de Contraseña - Sistema de Gestión");
            
            // Cuerpo del correo
            String link = "http://localhost:8080/password/reset?token=" + token + "&id=" + id;
            String contenido = "Hola,\n\nSe te a registrado como profesor de nuestra institucion. "
                             + "Este mensaje es para confirmar su usuario dentro del sistema, además "
                             + "debe crear una contraseña personal. Por favor, haz clic en el siguiente "
                             + "enlace para crear una:\n\n"
                             + link + "\n\nEste enlace expirará en 24 horas.";
            
            message.setText(contenido);

            // 4. Enviamos el correo
            Transport.send(message);
            System.out.println("Correo de recuperación enviado exitosamente a: " + correo_destinatario);

        } catch (MessagingException e) {
            // Si el correo rebota o no hay internet, capturamos el error
            e.printStackTrace();
            throw new RuntimeException("Error crítico al intentar enviar el correo.");
        }
    }
}