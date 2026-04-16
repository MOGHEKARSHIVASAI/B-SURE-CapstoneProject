package org.hartford.binsure.service;

import org.hartford.binsure.entity.Claim;
import org.hartford.binsure.entity.PolicyApplication;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.model.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.MimeTypeUtils;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Autowired(required = false)
    private VertexAiGeminiChatModel chatModel;

    @Autowired(required = false)
    private OpenTelemetry openTelemetry;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path root = Paths.get("uploads");

    private static final String OPENINFERENCE_SPAN_KIND     = "openinference.span.kind";
    private static final String LLM_INPUT_MESSAGES          = "llm.input_messages.0.message.content";
    private static final String LLM_OUTPUT_MESSAGES         = "llm.output_messages.0.message.content";
    private static final String LLM_MODEL_NAME              = "llm.model_name";
    private static final String ENTITY_ID_ATTR              = "entity.id";

    public void analyzeClaim(Claim claim) {
        if (chatModel == null) return;

        String promptText = String.format(
            "Analyze the following insurance claim for potential fraud and risk. " +
            "Return the result ONLY as a JSON object with keys: 'riskScore' (0-100), 'analysis' (string summary).\n\n" +
            "Claim Details (Today's Date: %s):\n" +
            "- Claim Number: %s\n" +
            "- Incident Date: %s\n" +
            "- Claimed Amount: %s\n" +
            "- Incident Description: %s\n" +
            "- Policy Details: %s\n",
            LocalDate.now(),
            claim.getClaimNumber(),
            claim.getIncidentDate(),
            claim.getClaimedAmount(),
            claim.getIncidentDescription(),
            claim.getPolicy() != null ? claim.getPolicy().getProductName() : "Unknown Policy"
        );
        
        List<Media> mediaList = new ArrayList<>();
        if (claim.getDocuments() != null) {
            for (org.hartford.binsure.entity.Document doc : claim.getDocuments()) {
                if ("CLAIM_PHOTO".equals(doc.getDocumentType().name())) {
                    try {
                        Path filePath = root.resolve(doc.getFilePath());
                        mediaList.add(new Media(MimeTypeUtils.parseMimeType(doc.getFileType()), new FileSystemResource(filePath)));
                    } catch (Exception e) {
                        System.err.println("Error loading image for AI analysis: " + doc.getFileName());
                    }
                }
            }
        }

        try {
            UserMessage userMessage = mediaList.isEmpty() 
                ? new UserMessage(promptText) 
                : new UserMessage(promptText, mediaList);

            String response;
            if (openTelemetry != null) {
                // Instrument with OTel for Arize
                Tracer tracer = openTelemetry.getTracer("binsure-ai-service", "1.0.0");
                Span span = tracer.spanBuilder("ai.analyze_claim.llm.call")
                        .setSpanKind(SpanKind.CLIENT)
                        .startSpan();

                try (Scope scope = span.makeCurrent()) {
                    span.setAttribute(OPENINFERENCE_SPAN_KIND, "LLM");
                    span.setAttribute(LLM_MODEL_NAME, "gemini-2.5-flash");
                    span.setAttribute(ENTITY_ID_ATTR, String.valueOf(claim.getId()));
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
                // Fallback if not configured
                response = chatModel.call(new Prompt(userMessage)).getResult().getOutput().getText();
            }

            // Robust cleaning for JSON
            if (response.contains("{")) {
                response = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
            }
            response = response.replaceAll("```json", "").replaceAll("```", "").trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            if (result.get("riskScore") != null) {
                claim.setRiskScore(((Number) result.get("riskScore")).intValue());
            }
            claim.setRiskAnalysis((String) result.get("analysis"));
        } catch (Exception e) {
            System.err.println("Error analyzing claim: " + e.getMessage());
            claim.setRiskAnalysis("AI Analysis failed: " + e.getMessage());
            claim.setRiskScore(0);
        }
    }

    public void assessUnderwritingRisk(PolicyApplication application) {
        if (chatModel == null) return;

        String promptText = String.format(
            "Assess the underwriting risk for the following insurance application. " +
            "Return the result ONLY as a JSON object with keys: 'aiRiskScore' (0-100), 'aiUnderwritingAnalysis' (string), 'recommendedPremium' (number).\n\n" +
            "Application Details (Today's Date: %s):\n" +
            "- Business: %s\n" +
            "- Industry: %s\n" +
            "- Product: %s\n" +
            "- Coverage Amount: %s\n" +
            "- Risk Notes: %s\n",
            LocalDate.now(),
            application.getBusiness() != null ? application.getBusiness().getCompanyName() : "Unknown Business",
            application.getBusiness() != null ? application.getBusiness().getIndustryType() : "Unknown Industry",
            application.getProduct() != null ? application.getProduct().getProductName() : "Unknown Product",
            application.getCoverageAmount(),
            application.getRiskNotes()
        );

        try {
            String response;
            if (openTelemetry != null) {
                // Instrument with OTel for Arize
                Tracer tracer = openTelemetry.getTracer("binsure-ai-service", "1.0.0");
                Span span = tracer.spanBuilder("ai.assess_risk.llm.call")
                        .setSpanKind(SpanKind.CLIENT)
                        .startSpan();

                try (Scope scope = span.makeCurrent()) {
                    span.setAttribute(OPENINFERENCE_SPAN_KIND, "LLM");
                    span.setAttribute(LLM_MODEL_NAME, "gemini-2.5-flash");
                    span.setAttribute(ENTITY_ID_ATTR, String.valueOf(application.getId()));
                    span.setAttribute(LLM_INPUT_MESSAGES, promptText);

                    response = chatModel.call(new Prompt(new UserMessage(promptText))).getResult().getOutput().getText();
                    
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
                response = chatModel.call(new Prompt(new UserMessage(promptText))).getResult().getOutput().getText();
            }

            if (response.contains("{")) {
                response = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
            }
            response = response.replaceAll("```json", "").replaceAll("```", "").trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            if (result.get("aiRiskScore") != null) {
                application.setAiRiskScore(((Number) result.get("aiRiskScore")).intValue());
            }
            application.setAiUnderwritingAnalysis((String) result.get("aiUnderwritingAnalysis"));
            Object recPremium = result.get("recommendedPremium");
            if (recPremium instanceof Number) {
                application.setRecommendedPremium(new java.math.BigDecimal(recPremium.toString()));
            }
        } catch (Exception e) {
            System.err.println("Error assessing risk: " + e.getMessage());
            application.setAiUnderwritingAnalysis("AI Assessment failed: " + e.getMessage());
            application.setAiRiskScore(0);
        }
    }
}
