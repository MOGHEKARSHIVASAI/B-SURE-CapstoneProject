package org.hartford.binsure.controller;

import org.hartford.binsure.dto.ChatRequest;
import org.hartford.binsure.dto.ChatResponse;
import org.hartford.binsure.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Please provide a valid message."));
        }
        ChatResponse response = chatbotService.askQuestion(request);
        return ResponseEntity.ok(response);
    }
}
