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
 *  OPCIÓN A — Google AI Studio (Gemini)
 *  Curso: Desarrollo Web Avanzado — Nivel 1
 * ══════════════════════════════════════════════════════════════════════
 *
 *  Gemini expone un endpoint COMPATIBLE con el formato OpenAI
 *  ChatCompletions, lo que hace que su integración sea muy similar
 *  a la de Groq.
 *
 *  Estructura de la petición (formato OpenAI ChatCompletions):
 *    POST /v1beta/openai/chat/completions
 *    {
 *      "model": "gemini-2.0-flash",
 *      "messages": [
 *        { "role": "system", "content": "instrucción del sistema" },
 *        { "role": "user",   "content": "pregunta del usuario" }
 *      ]
 *    }
 *
 *  Estructura de la respuesta:
 *    { "choices": [{ "message": { "content": "respuesta..." } }] }
 *
 *  Activación: llm.provider=gemini en application.properties
 *
 *  Prerequisito:
 *    1. Registrarse en https://ai.google.dev
 *    2. Crear API Key gratuita (sin tarjeta de crédito)
 *    3. En IntelliJ: Run → Edit Configurations → Environment Variables
 *       Agregar: GEMINI_API_KEY=AIzaSy...
 * ══════════════════════════════════════════════════════════════════════
 */
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "gemini")
public class GeminiDirectService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(GeminiDirectService.class);

    private final WebClient webClient;
    private final String model;

    public GeminiDirectService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.model}") String model) {

        this.model = model;

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        log.info("✅ GeminiDirectService inicializado | modelo: {} | baseUrl: {}", model, baseUrl);
    }

    @Override
    public String chat(String pregunta) {
        log.debug("→ Enviando pregunta a Gemini: '{}'", pregunta);

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
                        // Navegar la respuesta: choices[0].message.content
                        @SuppressWarnings("unchecked")
                        var choices = (List<Map<String, Object>>) resp.get("choices");
                        @SuppressWarnings("unchecked")
                        var message = (Map<String, Object>) choices.get(0).get("message");
                        return (String) message.get("content");
                    })
                    .block();

            log.debug("← Respuesta recibida de Gemini ({} chars)",
                    respuesta != null ? respuesta.length() : 0);
            return respuesta;

        } catch (WebClientResponseException e) {
            log.error("❌ Error HTTP {} desde Gemini: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Error al llamar a Gemini: " + e.getStatusCode()
                    + " — Verifica que tu GEMINI_API_KEY es válida.", e);
        }
    }

    @Override public String getNombreProveedor() { return "Google AI Studio (Gemini)"; }
    @Override public String getModelo()           { return model; }
}
