# Nivel 1 — API Directa REST → LLM
### Curso: Desarrollo Web Avanzado

---

## ¿Qué hace este proyecto?

Demuestra cómo consumir APIs de LLM **directamente con `WebClient`**, sin
frameworks especializados como Spring AI. Es el punto de partida del laboratorio:
entender qué ocurre "bajo el capó" antes de usar abstracciones.

---

## Estructura del proyecto

```
src/main/java/com/curso/llm/
├── Nivel1Application.java          ← Clase principal Spring Boot
├── controller/
│   ├── ChatController.java         ← Endpoint POST /api/v1/chat/directo
│   └── GlobalExceptionHandler.java ← Manejo de errores HTTP
├── dto/
│   ├── ChatRequest.java            ← { "pregunta": "..." }
│   └── ChatResponse.java           ← { "respuesta", "proveedor", "modelo", "tiempoMs" }
└── service/
    ├── LlmService.java             ← Interfaz común (chat, getNombreProveedor, getModelo)
    ├── GeminiDirectService.java    ← OPCIÓN A: Google AI Studio
    ├── GroqDirectService.java      ← OPCIÓN B: Groq
    └── OllamaDirectService.java    ← OPCIÓN C: Ollama local
```

---

## Cómo ejecutar

### Paso 1 — Elige tu proveedor

Edita `src/main/resources/application.properties`:

```properties
# Opciones: gemini | groq | ollama
llm.provider=gemini
```

### Paso 2 — Configura las credenciales

#### Opción A: Google Gemini
1. Ve a https://ai.google.dev y crea una API key gratuita
2. En IntelliJ: **Run → Edit Configurations → Environment Variables**
3. Agrega: `GEMINI_API_KEY=AIzaSy...`

#### Opción B: Groq
1. Ve a https://console.groq.com y crea una API key gratuita
2. ⚠️ Copia la key inmediatamente — Groq la muestra solo una vez
3. En IntelliJ: **Run → Edit Configurations → Environment Variables**
4. Agrega: `GROQ_API_KEY=gsk_...`

#### Opción C: Ollama (local, sin internet)
```bash
docker run -d -p 11434:11434 --name ollama ollama/ollama
docker exec ollama ollama pull llama3.2:1b
```

### Paso 3 — Ejecutar la aplicación

```bash
# Desde IntelliJ: botón Run en Nivel1Application.java
# O desde terminal:
./mvnw spring-boot:run
```

### Paso 4 — Probar

```bash
# Verificar qué proveedor está activo
curl http://localhost:8080/api/v1/chat/info

# Hacer una pregunta
curl -X POST http://localhost:8080/api/v1/chat/directo \
  -H 'Content-Type: application/json' \
  -d '{"pregunta": "¿Qué es Spring Boot en 2 oraciones?"}'
```

#### Respuesta esperada:
```json
{
  "respuesta": "Spring Boot es un framework Java que...",
  "proveedor": "Google AI Studio (Gemini)",
  "modelo": "gemini-2.0-flash",
  "tiempoMs": 2341
}
```

---

## Ejercicio comparativo

Cambia `llm.provider` entre `gemini`, `groq` y `ollama` y observa:

| Proveedor | Tiempo respuesta | Calidad | ¿Qué cambió en el código? |
|-----------|-----------------|---------|--------------------------|
| Gemini    | ~2–5 s          | Alta    | Solo la configuración     |
| Groq      | < 1 s           | Alta    | Solo la configuración     |
| Ollama    | ~5–30 s (CPU)   | Media   | Solo la configuración     |

**Pregunta de reflexión:** El controlador `ChatController.java` no cambió en ningún caso.
¿Por qué? ¿Cómo se llama ese principio de diseño?
*(Respuesta en el Nivel 2 con Spring AI)*

---

## Errores frecuentes

| Error | Causa | Solución |
|-------|-------|----------|
| `COLOCA-TU-KEY-AQUI` en logs | Variable de entorno no configurada | Agregar env var en IntelliJ |
| `401 Unauthorized` | API key inválida o expirada | Verificar la key en el portal del proveedor |
| `Connection refused: 11434` | Ollama no está corriendo | `docker ps` y revisar el contenedor |
| `Model not found` | Modelo no descargado | `docker exec ollama ollama pull llama3.2:1b` |
