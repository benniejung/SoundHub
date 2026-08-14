package com.yebin.sideproject.domain.chatbot.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessage(String id, MessageRole role, String content, LocalDateTime timestamp) {

    public static ChatMessage of(MessageRole role, String content) {
        return new ChatMessage(UUID.randomUUID().toString(), role, content, LocalDateTime.now());
    }
}
