package org.hartford.binsure.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final ChatModel chatModel;

    public ChatbotService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String chat(String userMessage) {
        return chatModel.call(userMessage);
    }
}
