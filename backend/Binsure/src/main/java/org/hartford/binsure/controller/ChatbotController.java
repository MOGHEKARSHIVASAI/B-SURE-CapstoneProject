package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hartford.binsure.dto.ChatRequest;
import org.hartford.binsure.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chatbot")
@Tag(name = "Chatbot", description = "AI-powered insurance assistant backed by Vertex AI Gemini")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a message to the AI chatbot",
               description = "Forwards the user message to the Vertex AI Gemini model and returns the response.")
    public ResponseEntity<Map<String, String>> chat(@Valid @RequestBody ChatRequest request) {
        String reply = chatbotService.chat(request.getMessage());
        return ResponseEntity.ok(Map.of("response", reply));
    }
}
