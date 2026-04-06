package org.hartford.binsure.service;

import org.hartford.binsure.entity.Document;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DocumentAnalysisService {

    @Autowired(required = false)
    private VertexAiGeminiChatModel chatModel;

    private final Path root = Paths.get("uploads");

    public void extractFeatures(Document document) {
        if (chatModel == null) return;

        try {
            Path filePath = root.resolve(document.getFilePath());
            FileSystemResource resource = new FileSystemResource(filePath);

            String promptText = "Extract important features (dates, amounts, names, purposes) from this document. " +
                                "Return the result as a concise JSON string.";

            // Using Spring AI's Media support for multimodal prompts
            Media media = new Media(MimeTypeUtils.parseMimeType(document.getFileType()), resource);
            UserMessage userMessage = new UserMessage(promptText, List.of(media));
            
            String response = chatModel.call(new Prompt(userMessage)).getResult().getOutput().getText();
            // Robust cleaning for JSON
            if (response.contains("{")) {
                response = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
            }
            response = response.replaceAll("```json", "").replaceAll("```", "").trim();
            document.setExtractedData(response);
        } catch (Exception e) {
            System.err.println("Error extracting document features: " + e.getMessage());
            document.setExtractedData("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
