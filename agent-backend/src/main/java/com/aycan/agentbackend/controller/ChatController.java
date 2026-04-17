package com.aycan.agentbackend.controller;

import com.aycan.agentbackend.dto.ChatRequest;
import com.aycan.agentbackend.dto.ChatResponse;
import com.aycan.agentbackend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.ask(request.getMessage());
        return ResponseEntity.ok(response);
    }
}