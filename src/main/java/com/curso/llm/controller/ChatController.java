package com.curso.llm.controller;

import com.curso.llm.dto.ChatRequest;
import com.curso.llm.dto.ChatResponse;
import com.curso.llm.service.LlmService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST — Nivel 1
 * Endpoints:
 *   POST /api/v1/chat/directo  → envía una pregunta al LLM activo
 *   GET  /api/v1/chat/info     → muestra qué proveedor está configurado
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final LlmService llmService;

    // Constructor explícito en lugar de @RequiredArgsConstructor de Lombok
    public ChatController(LlmService llmService) {
        this.llmService = llmService;
    }

    /**
     * Envía una pregunta al LLM activo y retorna la respuesta.
     *
     * Ejemplo:
     *   curl -X POST http://localhost:8080/api/v1/chat/directo \
     *        -H 'Content-Type: application/json' \
     *        -d '{"pregunta": "¿Qué es Spring Boot en 2 oraciones?"}'
     */
    @PostMapping("/directo")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("📥 Pregunta recibida para proveedor: {}", llmService.getNombreProveedor());

        long inicio = System.currentTimeMillis();
        String respuesta = llmService.chat(request.pregunta());
        long tiempoMs = System.currentTimeMillis() - inicio;

        log.info("📤 Respuesta generada en {} ms", tiempoMs);

        return ResponseEntity.ok(new ChatResponse(
                respuesta,
                llmService.getNombreProveedor(),
                llmService.getModelo(),
                tiempoMs
        ));
    }

    /**
     * Verifica qué proveedor está activo antes de hacer preguntas.
     *
     * Ejemplo:
     *   curl http://localhost:8080/api/v1/chat/info
     */
    @GetMapping("/info")
    public ResponseEntity<ChatResponse> info() {
        return ResponseEntity.ok(new ChatResponse(
                "Proveedor activo y listo para recibir preguntas.",
                llmService.getNombreProveedor(),
                llmService.getModelo(),
                0
        ));
    }
}
