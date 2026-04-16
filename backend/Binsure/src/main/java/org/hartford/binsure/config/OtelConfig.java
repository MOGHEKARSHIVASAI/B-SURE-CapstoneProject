package org.hartford.binsure.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtelConfig {

    private static final String ARIZE_ENDPOINT = "https://otlp.arize.com/v1/traces";
    private static final String ARIZE_API_KEY  = "ak-348159f3-72cb-47bf-8988-7ff4e3ee1f71-plQ3cfV2M3jBT2VbPDzw02ePDANkghBc";
    private static final String ARIZE_SPACE_ID = "U3BhY2U6NDE4MDk6d05Xdg==";

    @Bean
    public OpenTelemetry openTelemetry() {

        // 1. OTLP/HTTP exporter → Arize Cloud
        OtlpHttpSpanExporter exporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(ARIZE_ENDPOINT)
                .addHeader("Authorization", "Bearer " + ARIZE_API_KEY)
                .addHeader("space_id", ARIZE_SPACE_ID)
                .build();

        // 2. Resource attributes (visible in Arize as project metadata)
        //    Using plain string keys to avoid semconv alpha dependency
        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.builder()
                        .put(AttributeKey.stringKey("service.name"), "binsure-chatbot")
                        .put(AttributeKey.stringKey("service.version"), "1.0.0")
                        .put(AttributeKey.stringKey("deployment.environment"), "development")
                        .put(AttributeKey.stringKey("model_id"), "binsure-chatbot-model")
                        .put(AttributeKey.stringKey("arize.project.name"), "binsure-chatbot")
                        .build())
        );

        // 3. Tracer provider with batch export
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
                .build();

        // 4. Build and attempt to register as global OTel instance
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
                
        try {
            GlobalOpenTelemetry.set(sdk);
        } catch (IllegalStateException e) {
            // Already registered during hot-reload / devtools restart
            // Safe to ignore and use the existing global instance or bean
        }

        // Flush traces on JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

        return sdk;
    }
}
