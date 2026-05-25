package com.lab.mock_service;

import com.lab.mock_service.model.LabResult;
import com.lab.mock_service.service.LabResultGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LabResultGeneratorTest {

    private LabResultGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new LabResultGenerator();
    }

    @Test
    void testGenerate_ReturnsNotNull() {
        Object result = generator.generate();
        assertNotNull(result);
    }

    @Test
    void testNormalScenario_AllValuesInRange() {
        LabResult result = (LabResult) invokeScenario(0);
        assertEquals("NORMAL", result.getScenario());
        result.getTests().forEach(test -> {
            assertTrue(test.getValue() >= test.getReferenceMin());
            assertTrue(test.getValue() <= test.getReferenceMax());
        });
    }

    @Test
    void testAbnormalHighScenario_GlucoseHigh() {
        LabResult result = (LabResult) invokeScenario(1);
        assertEquals("ABNORMAL_HIGH", result.getScenario());
        result.getTests().stream()
                .filter(t -> t.getName().equals("Glucose"))
                .findFirst()
                .ifPresent(t -> assertTrue(t.getValue() > t.getReferenceMax()));
    }

    @Test
    void testCriticalScenario_AllValuesAbnormal() {
        LabResult result = (LabResult) invokeScenario(2);
        assertEquals("CRITICAL", result.getScenario());
        result.getTests().forEach(test ->
                assertTrue(test.getValue() < test.getReferenceMin() || test.getValue() > test.getReferenceMax())
        );
    }

    @Test
    void testMissingFieldScenario_NoPatientId() {
        Object result = invokeScenario(3);
        assertInstanceOf(Map.class, result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("INVALID_MISSING_FIELDS", map.get("scenario"));
        assertFalse(map.containsKey("patientId"));
    }

    @Test
    void testAbnormalLowScenario_GlucoseLow() {
        LabResult result = (LabResult) invokeScenario(4);
        assertEquals("ABNORMAL_LOW", result.getScenario());
        result.getTests().stream()
                .filter(t -> t.getName().equals("Glucose"))
                .findFirst()
                .ifPresent(t -> assertTrue(t.getValue() < t.getReferenceMin()));
    }

    @Test
    void testEmptyTestsScenario() {
        Object result = invokeScenario(5);
        assertInstanceOf(Map.class, result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("INVALID_EMPTY_TESTS", map.get("scenario"));
    }

    @Test
    void testNegativeValueScenario() {
        Object result = invokeScenario(6);
        assertInstanceOf(Map.class, result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("INVALID_NEGATIVE_VALUE", map.get("scenario"));
    }

    @Test
    void testDuplicateTestScenario() {
        Object result = invokeScenario(7);
        assertInstanceOf(Map.class, result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("INVALID_DUPLICATE_TEST", map.get("scenario"));
    }

    @RepeatedTest(20)
    void testGenerate_AlwaysReturnsValidObject() {
        Object result = generator.generate();
        assertNotNull(result);
    }

    // switch case'deki her senaryoyu direkt çağırmak için reflection kullanıyoruz
    private Object invokeScenario(int scenarioIndex) {
        try {
            var method = LabResultGenerator.class.getDeclaredMethod("generate");
            // Random'u override edemediğimiz için generate'i çok çağırıp istediğimiz senaryoyu buluyoruz
            for (int i = 0; i < 1000; i++) {
                Object result = generator.generate();
                if (result instanceof LabResult lr && getExpectedScenario(scenarioIndex).equals(lr.getScenario())) {
                    return result;
                }
                if (result instanceof Map<?, ?> map && getExpectedScenario(scenarioIndex).equals(map.get("scenario"))) {
                    return result;
                }
            }
            fail("Scenario " + scenarioIndex + " not generated in 1000 attempts");
            return null;
        } catch (Exception e) {
            fail("Error: " + e.getMessage());
            return null;
        }
    }

    private String getExpectedScenario(int index) {
        return switch (index) {
            case 0 -> "NORMAL";
            case 1 -> "ABNORMAL_HIGH";
            case 2 -> "CRITICAL";
            case 3 -> "INVALID_MISSING_FIELDS";
            case 4 -> "ABNORMAL_LOW";
            case 5 -> "INVALID_EMPTY_TESTS";
            case 6 -> "INVALID_NEGATIVE_VALUE";
            case 7 -> "INVALID_DUPLICATE_TEST";
            default -> "";
        };
    }
}