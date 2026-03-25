package com.curso.llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 *  Nivel 1 — API Directa REST → LLM
 *  Curso:  Desarrollo Web Avanzado
 *
 *  Demuestra el consumo directo de APIs de LLM usando WebClient,
 *  sin frameworks especializados como Spring AI.
 *
 *  Proveedores soportados:
 *    A) Google AI Studio (Gemini)  → api.key en: GEMINI_API_KEY
 *    B) Groq                       → api.key en: GROQ_API_KEY
 *    C) Ollama local               → sin key, requiere Docker
 *
 *  Para cambiar de proveedor: modificar llm.provider en application.properties
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
public class Nivel1Application {

    public static void main(String[] args) {
        SpringApplication.run(Nivel1Application.class, args);
    }
}
