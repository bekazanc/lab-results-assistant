package com.lab.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.lab.backend.dto.LabResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public String analyze(LabResultDto result) {
        try {
            String prompt = buildPrompt(result);

            Map<String, Object> request = new HashMap<>();
            request.put("model", "llama3");
            request.put("prompt", prompt);
            request.put("stream", false);

            String response = restTemplate.postForObject(OLLAMA_URL, request, String.class);
            JsonNode node = objectMapper.readTree(response);
            return node.path("response").asText("Yorum alınamadı.");

        } catch (Exception e) {
            log.error("Ollama error: {}", e.getMessage());
            return "LLM servisi şu an kullanılamıyor: " + e.getMessage();
        }
    }

    private String buildPrompt(LabResultDto result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sen bir klinik laboratuvar uzmanısın. ");
        sb.append("Aşağıdaki kan testi sonuçlarını değerlendir. ");
        sb.append("Anormal değerleri belirt ve kısa bir klinik yorum yap. ");
        sb.append("Türkçe yanıt ver.\n\n");
        sb.append("Hasta ID: ").append(result.getPatientId()).append("\n");
        sb.append("Cihaz: ").append(result.getDeviceId()).append("\n");
        sb.append("Durum: ").append(result.getStatus()).append("\n\n");
        sb.append("Test Sonuçları:\n");

        result.getTests().forEach(t -> {
            sb.append("- ").append(t.getName())
                    .append(": ").append(t.getValue())
                    .append(" ").append(t.getUnit())
                    .append(" (Normal: ").append(t.getReferenceMin())
                    .append("-").append(t.getReferenceMax()).append(")")
                    .append(t.getIsAbnormal() ? " ⚠️ ANORMAL" : " ✓")
                    .append("\n");
        });

        return sb.toString();
    }
}