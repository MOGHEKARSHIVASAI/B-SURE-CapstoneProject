package org.hartford.binsure.service;

import org.hartford.binsure.dto.ChatRequest;
import org.hartford.binsure.dto.ChatResponse;
import org.hartford.binsure.entity.Business;
import org.hartford.binsure.entity.Claim;
import org.hartford.binsure.entity.InsuranceProduct;
import org.hartford.binsure.entity.Policy;
import org.hartford.binsure.entity.PolicyApplication;
import org.hartford.binsure.repository.BusinessRepository;
import org.hartford.binsure.repository.ClaimRepository;
import org.hartford.binsure.repository.InsuranceProductRepository;
import org.hartford.binsure.repository.PolicyApplicationRepository;
import org.hartford.binsure.repository.PolicyRepository;
import org.hartford.binsure.security.SecurityUtils;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// OpenTelemetry – direct SDK (manual instrumentation for Arize Phoenix)
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired(required = false)
    private VertexAiGeminiChatModel chatModel;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private InsuranceProductRepository productRepository;

    @Autowired
    private PolicyApplicationRepository applicationRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private OpenTelemetry openTelemetry;

    // OpenInference semantic convention attributes for Arize
    private static final String OPENINFERENCE_SPAN_KIND     = "openinference.span.kind";
    private static final String LLM_INPUT_MESSAGES          = "llm.input_messages.0.message.content";
    private static final String LLM_OUTPUT_MESSAGES         = "llm.output_messages.0.message.content";
    private static final String LLM_MODEL_NAME              = "llm.model_name";
    private static final String USER_ID_ATTR                = "user.id";
    private static final String USER_ROLE_ATTR              = "user.role";
    private static final String SESSION_ID_ATTR             = "session.id";

    private static final String SYSTEM_PROMPT = """
            You are 'B-SURE Assistant', a highly professional, helpful, and concise AI assistant for the B-SURE business insurance platform.
            Answer the user's questions strictly based on the insurance context provided below. If the user asks general questions about B-SURE, answer creatively emphasizing that B-SURE is a modern business insurance platform providing liability, property, D&O, and cyber insurance. 
            Do NOT provide details about information the user DOES NOT own. If the answer is not in the context, say you do not have that specific information. Keep answers under 150 words.

            User Context:
            Name: {userName}
            Role: {userRole}
            
            User's Business Portfolio:
            {businessContext}
            
            User's Active Policies:
            {policiesContext}
            
            User's Policy Applications:
            {applicationsContext}
            
            User's Recent Claims:
            {claimsContext}
            
            Available Insurance Products for Recommendation:
            {productsContext}
            """;

    public ChatResponse askQuestion(ChatRequest request) {
        // Check if chatModel is available (GCP credentials configured)
        if (chatModel == null) {
            return new ChatResponse("Chatbot service is currently unavailable. Google Cloud Platform credentials are not configured or invalid. Please contact the administrator.");
        }

        Long userId = securityUtils.getCurrentUserId();
        String userName = securityUtils.getCurrentUser().getFirstName() + " " + securityUtils.getCurrentUser().getLastName();
        String userRole = securityUtils.getCurrentUser().getRole().name();

        // ── Arize Phoenix: create manual OTel LLM span ─────────────────────────
        Tracer tracer = openTelemetry.getTracer("binsure-chatbot", "1.0.0");
        Span span = tracer.spanBuilder("chatbot.llm.call")
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {

            // OpenInference attributes (required for Arize LLM tracing)
            span.setAttribute(OPENINFERENCE_SPAN_KIND, "LLM");
            span.setAttribute(LLM_MODEL_NAME, "gemini-2.5-flash");
            span.setAttribute(USER_ID_ATTR,   String.valueOf(userId));
            span.setAttribute(USER_ROLE_ATTR, userRole);
            span.setAttribute(SESSION_ID_ATTR, String.valueOf(request.getMessage().hashCode()));

            // Record the user prompt as the LLM input
            span.setAttribute(LLM_INPUT_MESSAGES, request.getMessage());

            // 1. Fetch Business Portfolio
            List<Business> businesses = businessRepository.findByUser_Id(userId);
            String businessContext = businesses.isEmpty() ? "No registered businesses." :
                    businesses.stream()
                            .map(b -> String.format("- %s (Industry: %s)", b.getCompanyName(), b.getIndustryType()))
                            .collect(Collectors.joining("\n"));

            // 2. Fetch Policies
            List<Policy> policies = policyRepository.findByBusiness_User_Id(userId);
            String policiesContext = policies.isEmpty() ? "No active policies." :
                    policies.stream()
                            .map(p -> String.format("- %s: Policy #%s | Status: %s | Premium: INR %s", p.getProductName(), p.getPolicyNumber(), p.getStatus(), p.getAnnualPremium()))
                            .collect(Collectors.joining("\n"));

            // 3. Fetch Applications
            List<PolicyApplication> applications = applicationRepository.findByBusiness_User_Id(userId);
            String applicationsContext = applications.isEmpty() ? "No pending applications." :
                    applications.stream()
                            .map(a -> String.format("- Application for %s | Status: %s | Date: %s", a.getProductName(), a.getStatus(), a.getCreatedAt()))
                            .collect(Collectors.joining("\n"));

            // 4. Fetch Claims
            List<Claim> claims = claimRepository.findByBusiness_User_Id(userId);
            String claimsContext = claims.isEmpty() ? "No recent claims." :
                    claims.stream()
                            .map(c -> String.format("- Claim #%s | Amount: INR %s | Status: %s | Date: %s", c.getClaimNumber(), c.getClaimedAmount(), c.getStatus(), c.getClaimDate()))
                            .collect(Collectors.joining("\n"));

            // 5. Fetch Available Products
            List<InsuranceProduct> products = productRepository.findByIsActiveTrue();
            String productsContext = products.isEmpty() ? "No products available at this time." :
                    products.stream()
                            .map(p -> String.format("- %s (%s): %s", p.getProductName(), p.getProductCode(), p.getDescription()))
                            .collect(Collectors.joining("\n"));

            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT);
            Message systemMessage = systemPromptTemplate.createMessage(java.util.Map.of(
                    "userName",           userName,
                    "userRole",           userRole,
                    "businessContext",    businessContext,
                    "policiesContext",    policiesContext,
                    "applicationsContext",applicationsContext,
                    "claimsContext",       claimsContext,
                    "productsContext",    productsContext
            ));

            UserMessage userMessage = new UserMessage(request.getMessage());
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            String aiResponse = chatModel.call(prompt).getResult().getOutput().getText();

            // Record the LLM response as the output
            span.setAttribute(LLM_OUTPUT_MESSAGES, aiResponse);
            span.setStatus(StatusCode.OK);

            return new ChatResponse(aiResponse);

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            System.err.println("Chatbot API Error: " + e.getMessage());
            e.printStackTrace();
            return new ChatResponse("Google Cloud Authentication Failed: " + e.getMessage());
        } finally {
            span.end(); // always end the span
        }
    }
}
