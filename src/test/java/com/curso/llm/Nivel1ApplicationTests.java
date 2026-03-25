package com.curso.llm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifica que el contexto de Spring carga correctamente
 * con el proveedor gemini como valor por defecto para los tests.
 *
 * Para probar un proveedor específico, agrega la anotación:
 *   @TestPropertySource(properties = "llm.provider=groq")
 */
@SpringBootTest
@TestPropertySource(properties = {
        "llm.provider=gemini",
        "gemini.api.key=test-key-placeholder"
})
class Nivel1ApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring inicia sin errores
    }
}
