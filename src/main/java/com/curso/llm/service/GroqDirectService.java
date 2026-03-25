package com.curso.llm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * ══════════════════════════════════════════════════════════════════════
 *  OPCIÓN B — Groq
 *  Curso: Desarrollo Web Avanzado — Nivel 1
 * ══════════════════════════════════════════════════════════════════════
 *
 *  OBSERVACIÓN PEDAGÓGICA CLAVE:
 *  Compara este servicio con GeminiDirectService línea a línea.
 *  Son prácticamente idénticos — solo cambia la URL base y el modelo.
 *  Groq implementa el estándar OpenAI ChatCompletions al 100%.
 *
 *  Activación: llm.provider=groq en application.properties
 *
 *  Prerequisito:
 *    1. Registrarse en https://console.groq.com (con Google o GitHub)
 *    2. API Keys → Create API Key
 *    3. ⚠️ Copiar la key inmediatamente — Groq la muestra solo una vez
 *    4. En IntelliJ: Run → Edit Configurations → Environment Variables
 *       Agregar: GROQ_API_KEY=gsk_...
 * ══════════════════════════════════════════════════════════════════════
 */
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "groq")
public class GroqDirectService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(GroqDirectService.class);

    private final WebClient webClient;
    private final String model;

    public GroqDirectService(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.base-url}") String baseUrl,
            @Value("${groq.model}") String model) {

        this.model = model;

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        log.info("✅ GroqDirectService inicializado | modelo: {} | baseUrl: {}", model, baseUrl);
    }

    @Override
    public String chat(String pregunta) {
        log.debug("→ Enviando pregunta a Groq: '{}'", pregunta);

        // OBSERVA: body idéntico al de GeminiDirectService
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system",
                               "content", "Eres un asistente útil y didáctico. "
                                        + "Responde siempre en español, de forma clara y concisa."),
                        Map.of("role", "user", "content", pregunta)
                )
        );

        try {
            String respuesta = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(resp -> {
                        // Misma navegación que Gemini: choices[0].message.content
                        @SuppressWarnings("unchecked")
                        var choices = (List<Map<String, Object>>) resp.get("choices");
                        @SuppressWarnings("unchecked")
                        var message = (Map<String, Object>) choices.get(0).get("message");
                        return (String) message.get("content");
                    })
                    .block();

            log.debug("← Respuesta recibida de Groq ({} chars)",
                    respuesta != null ? respuesta.length() : 0);
            return respuesta;

        } catch (WebClientResponseException e) {
            log.error("❌ Error HTTP {} desde Groq: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Error al llamar a Groq: " + e.getStatusCode()
                    + " — Verifica que tu GROQ_API_KEY es válida.", e);
        }
    }

    @Override public String getNombreProveedor() { return "Groq"; }
    @Override public String getModelo()           { return model; }
}
