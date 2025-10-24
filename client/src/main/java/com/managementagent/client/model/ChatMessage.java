package com.managementagent.client.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty teamId = new SimpleLongProperty();
    private final LongProperty senderId = new SimpleLongProperty();
    private final StringProperty senderName = new SimpleStringProperty();
    private final StringProperty message = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> sentAt = new SimpleObjectProperty<>();

    public long getId() {
        return id.get();
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public long getTeamId() {
        return teamId.get();
    }

    public void setTeamId(long teamId) {
        this.teamId.set(teamId);
    }

    public long getSenderId() {
        return senderId.get();
    }

    public void setSenderId(long senderId) {
        this.senderId.set(senderId);
    }

    public String getSenderName() {
        return senderName.get();
    }

    public void setSenderName(String senderName) {
        this.senderName.set(senderName);
    }

    public String getMessage() {
        return message.get();
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public LocalDateTime getSentAt() {
        return sentAt.get();
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt.set(sentAt);
    }

    public StringProperty messageProperty() {
        return message;
    }

    public ObjectProperty<LocalDateTime> sentAtProperty() {
        return sentAt;
    }

    @Override
    public String toString() {
        LocalDateTime time = getSentAt();
        String formatted = time != null ? FORMATTER.format(time) : "";
        return formatted + " - " + getSenderName() + ": " + getMessage();
    }
}
