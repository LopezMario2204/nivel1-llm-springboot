package com.curso.llm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * ══════════════════════════════════════════════════════════════════════
 *  OPCIÓN C — Ollama (modelo local)
 *  Curso: Desarrollo Web Avanzado — Nivel 1
 * ══════════════════════════════════════════════════════════════════════
 *
 *  DIFERENCIA CLAVE respecto a Gemini y Groq:
 *  Ollama usa su propia API REST, diferente del estándar OpenAI.
 *
 *    Gemini/Groq → endpoint: /chat/completions
 *                  campo entrada:  "messages" (lista de roles)
 *                  campo salida:   choices[0].message.content
 *
 *    Ollama      → endpoint: /api/generate
 *                  campo entrada:  "prompt" (texto plano)
 *                  campo salida:   "response" (campo directo)
 *
 *  Activación: llm.provider=ollama en application.properties
 *
 *  Prerequisito (Docker debe estar corriendo):
 *    docker run -d -p 11434:11434 --name ollama ollama/ollama
 *    docker exec ollama ollama pull llama3.2:1b
 * ══════════════════════════════════════════════════════════════════════
 */
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "ollama")
public class OllamaDirectService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(OllamaDirectService.class);

    private final WebClient webClient;
    private final String model;

    public OllamaDirectService(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String model) {

        this.model = model;

        // Sin header de Authorization — Ollama local no requiere autenticación
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();

        log.info("✅ OllamaDirectService inicializado | modelo: {} | baseUrl: {}", model, baseUrl);
    }

    @Override
    public String chat(String pregunta) {
        log.debug("→ Enviando pregunta a Ollama: '{}'", pregunta);

        // Formato propio de Ollama — diferente al de Gemini/Groq
        Map<String, Object> body = Map.of(
                "model",  model,
                "prompt", pregunta,  // campo "prompt", no "messages"
                "stream", false       // false = respuesta completa de una vez
        );

        try {
            String respuesta = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(resp -> (String) resp.get("response")) // campo directo "response"
                    .block();

            log.debug("← Respuesta recibida de Ollama ({} chars)",
                    respuesta != null ? respuesta.length() : 0);
            return respuesta;

        } catch (WebClientResponseException e) {
            log.error("❌ Error HTTP {} desde Ollama: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Error al llamar a Ollama: " + e.getStatusCode()
                    + " — ¿Está Docker corriendo con el contenedor ollama activo?", e);
        } catch (Exception e) {
            log.error("❌ Error de conexión con Ollama: {}", e.getMessage());
            throw new RuntimeException(
                    "No se pudo conectar con Ollama. "
                    + "Verifica: docker ps | grep ollama", e);
        }
    }

    @Override public String getNombreProveedor() { return "Ollama (local)"; }
    @Override public String getModelo()           { return model; }
}
