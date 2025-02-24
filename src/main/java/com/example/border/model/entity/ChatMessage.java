package com.example.border.model.entity;

import com.example.border.model.enums.MessageType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatMessage extends BaseEntity {

    private String content;
    private MessageType messageType;

    private LocalDateTime timestamp;
    private boolean isRead;

    @ManyToOne
    ChatRoom chatRoom;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    public ChatMessage(String content,
                       MessageType messageType,
                       LocalDateTime timestamp,
                       boolean isRead,
                       ChatRoom chatRoom,
                       User sender) {
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.chatRoom = chatRoom;
        this.sender = sender;
    }

    public ChatMessage() {

    }
}
