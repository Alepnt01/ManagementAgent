package com.managementagent.server.service;

import com.managementagent.server.ServerSettings;
import com.managementagent.server.dao.CollaborationDAO;
import com.managementagent.server.model.ClientContact;
import com.managementagent.server.model.EmailRequest;
import com.managementagent.server.model.Employee;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestisce la composizione e l'invio delle email in uscita.
 */
public class MailService {

    private final CollaborationDAO collaborationDAO;
    // Pool dedicato alla spedizione asincrona dei messaggi.
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Session mailSession;

    public MailService(CollaborationDAO collaborationDAO) {
        // Inietto il DAO per recuperare dipendenti e clienti dal database.
        this.collaborationDAO = collaborationDAO;
        // Creo la sessione SMTP secondo le impostazioni applicative.
        this.mailSession = buildSession();
    }

    public CompletableFuture<Void> sendEmailAsync(EmailRequest request) {
        // Esegue l'invio in un thread separato per non bloccare il controller REST.
        return CompletableFuture.runAsync(() -> sendEmail(request), executorService);
    }

    private void sendEmail(EmailRequest request) {
        if (request.getEmployeeId() == null || request.getClientId() == null) {
            // Validazione dei riferimenti obbligatori per l'invio dell'email.
            throw new IllegalArgumentException("Employee and client identifiers are required");
        }
        Optional<Employee> employeeOpt = collaborationDAO.findEmployeeById(request.getEmployeeId());
        Optional<ClientContact> clientOpt = collaborationDAO.findClientById(request.getClientId());
        Employee employee = employeeOpt.orElseThrow(() ->
                new IllegalArgumentException("Employee not found: " + request.getEmployeeId()));
        ClientContact client = clientOpt.orElseThrow(() ->
                new IllegalArgumentException("Client not found: " + request.getClientId()));

        if (employee.getEmail() == null || employee.getEmail().isBlank()) {
            throw new IllegalStateException("Employee email address is not configured");
        }
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new IllegalStateException("Client email address is not configured");
        }
        try {
            // Preparo il messaggio email utilizzando i dati recuperati dal database.
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(employee.getEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(client.getEmail()));
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            // Invio reale attraverso il transport SMTP.
            Transport.send(message);
            // Registro l'operazione per mantenere uno storico lato database.
            collaborationDAO.logEmail(employee.getId(), client.getId(), request.getSubject(), request.getBody());
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to send email", e);
        }
    }

    private Session buildSession() {
        String host = ServerSettings.getMailHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("SMTP host must be configured");
        }
        Properties props = new Properties();
        // Parametri principali per la connessione SMTP.
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", Integer.toString(ServerSettings.getMailPort()));
        props.put("mail.smtp.starttls.enable", Boolean.toString(ServerSettings.isMailStartTlsEnabled()));
        String fromAddress = ServerSettings.getMailFromAddress();
        if (fromAddress != null && !fromAddress.isBlank()) {
            props.put("mail.smtp.from", fromAddress);
        }

        String username = ServerSettings.getMailUsername();
        String password = ServerSettings.getMailPassword();
        boolean authenticated = username != null && !username.isBlank() && password != null && !password.isBlank();
        props.put("mail.smtp.auth", Boolean.toString(authenticated));

        if (authenticated) {
            // Sessione autenticata con le credenziali fornite.
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
        // Sessione senza autenticazione, utile per relay interni.
        return Session.getInstance(props);
    }

    public void shutdown() {
        // Rilascia le risorse del pool quando il servizio viene arrestato.
        executorService.shutdown();
    }
}
