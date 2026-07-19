package com.yebin.sideproject.domain.chatbot.controller;

import com.yebin.sideproject.domain.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OpenAIController {
    private final ChatService chatService;

    @GetMapping("/api/chat")
    public String chatGptV2(@RequestParam String msg) {
        return chatService.createChat(msg);
    }
}
