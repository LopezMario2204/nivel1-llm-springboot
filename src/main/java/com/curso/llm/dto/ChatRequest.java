package com.curso.llm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de chat.
 * El campo 'pregunta' es obligatorio y tiene un límite de longitud.
 */
public record ChatRequest(

        @NotBlank(message = "La pregunta no puede estar vacía")
        @Size(max = 1000, message = "La pregunta no puede superar los 1000 caracteres")
        String pregunta

) {}
