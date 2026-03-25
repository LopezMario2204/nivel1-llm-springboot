package com.curso.llm.dto;

/**
 * DTO para la respuesta del chat.
 * Incluye metadatos del proveedor para propósitos pedagógicos.
 */
public record ChatResponse(

        String respuesta,
        String proveedor,
        String modelo,
        long tiempoMs

) {}
