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

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DocumentAnalysisService {

    @Autowired(required = false)
    private VertexAiGeminiChatModel chatModel;

    @Autowired(required = false)
    private OpenTelemetry openTelemetry;

    private final Path root = Paths.get("uploads");

    private static final String OPENINFERENCE_SPAN_KIND     = "openinference.span.kind";
    private static final String LLM_INPUT_MESSAGES          = "llm.input_messages.0.message.content";
    private static final String LLM_OUTPUT_MESSAGES         = "llm.output_messages.0.message.content";
    private static final String LLM_MODEL_NAME              = "llm.model_name";
    private static final String ENTITY_ID_ATTR              = "entity.id";

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
            
            String response;
            if (openTelemetry != null) {
                Tracer tracer = openTelemetry.getTracer("binsure-document-service", "1.0.0");
                Span span = tracer.spanBuilder("ai.extract_features.llm.call")
                        .setSpanKind(SpanKind.CLIENT)
                        .startSpan();

                try (Scope scope = span.makeCurrent()) {
                    span.setAttribute(OPENINFERENCE_SPAN_KIND, "LLM");
                    span.setAttribute(LLM_MODEL_NAME, "gemini-2.5-flash");
                    span.setAttribute(ENTITY_ID_ATTR, String.valueOf(document.getId()));
                    span.setAttribute(LLM_INPUT_MESSAGES, promptText);

                    response = chatModel.call(new Prompt(userMessage)).getResult().getOutput().getText();
                    
                    span.setAttribute(LLM_OUTPUT_MESSAGES, response);
                    span.setStatus(StatusCode.OK);
                } catch (Exception e) {
                    span.setStatus(StatusCode.ERROR, e.getMessage());
                    span.recordException(e);
                    throw e;
                } finally {
                    span.end();
                }
            } else {
                response = chatModel.call(new Prompt(userMessage)).getResult().getOutput().getText();
            }

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
