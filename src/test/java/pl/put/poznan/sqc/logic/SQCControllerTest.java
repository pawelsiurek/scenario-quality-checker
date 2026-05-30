package pl.put.poznan.sqc.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.put.poznan.sqc.model.*;
import pl.put.poznan.sqc.rest.SQCController;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SQCControllerTest {
    @Mock
    private SQC sqc;

    @InjectMocks
    private SQCController controller;

    @Test
    void okResponse() {
        ScenarioOptions options = new ScenarioOptions(null, null, null, null, null, null, null);
        Scenario validScenario = new Scenario("Valid", List.of(), List.of(), new Step());
        ScenarioWrapper validPayload = new ScenarioWrapper(validScenario, options);

        AnalysisResponse successResponse = AnalysisResponse.builder()
                .status(ResponseStatus.SUCCESS)
                .build();

        when(sqc.analyze(validPayload)).thenReturn(successResponse);

        ResponseEntity<AnalysisResponse> response = controller.analyze(validPayload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        verify(sqc, times(1)).analyze(validPayload);
    }

    @Test
    void badRequestResponse() {
        ScenarioOptions options = new ScenarioOptions(null, null, null, null, null, null, null);
        Scenario validScenario = new Scenario("Valid", List.of(), List.of(), new Step());
        ScenarioWrapper validPayload = new ScenarioWrapper(validScenario, options);

        AnalysisResponse successResponse = AnalysisResponse.builder()
                .status(ResponseStatus.INPUT_ERROR)
                .build();

        when(sqc.analyze(validPayload)).thenReturn(successResponse);

        ResponseEntity<AnalysisResponse> response = controller.analyze(validPayload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(ResponseStatus.INPUT_ERROR);
        verify(sqc, times(1)).analyze(validPayload);
    }

    @Test
    void serverErrorResponse() {
        ScenarioOptions options = new ScenarioOptions(null, null, null, null, null, null, null);
        Scenario validScenario = new Scenario("Valid", List.of(), List.of(), new Step());
        ScenarioWrapper validPayload = new ScenarioWrapper(validScenario, options);

        AnalysisResponse successResponse = AnalysisResponse.builder()
                .status(ResponseStatus.SERVER_ERROR)
                .build();

        when(sqc.analyze(validPayload)).thenReturn(successResponse);

        ResponseEntity<AnalysisResponse> response = controller.analyze(validPayload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(ResponseStatus.SERVER_ERROR);
        verify(sqc, times(1)).analyze(validPayload);
    }

    @Test
    void analyzeTextOk() {
        ScenarioOptions options = new ScenarioOptions(null, null, null, null, null, null, null);
        Scenario validScenario = new Scenario("Valid", List.of(), List.of(), new Step());
        ScenarioWrapper validPayload = new ScenarioWrapper(validScenario, options);

        String expectedText = "1. First step\n2. Second step";
        when(sqc.analyzeToText(validPayload)).thenReturn(expectedText);

        ResponseEntity<String> response = controller.analyzeText(validPayload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedText);
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))
                .containsExactly("attachment; filename=\"scenario.txt\"");
        verify(sqc).analyzeToText(validPayload);
    }

    @Test
    void analyzeTextBadRequest() {
        ScenarioOptions options = new ScenarioOptions(null, null, null, null, null, null, null);
        Scenario validScenario = new Scenario("Valid", List.of(), List.of(), new Step());
        ScenarioWrapper validPayload = new ScenarioWrapper(validScenario, options);

        when(sqc.analyzeToText(validPayload)).thenReturn(null);

        ResponseEntity<String> response = controller.analyzeText(validPayload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Failed to generate text scenario");
        verify(sqc).analyzeToText(validPayload);
    }

    @Test
    void jsonRejectsNullInput() {
        ScenarioWrapper badPayload = null;

        ResponseEntity<AnalysisResponse> response = controller.analyze(badPayload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(ResponseStatus.INPUT_ERROR);
        verifyNoInteractions(sqc);
    }

    @Test
    void jsonRejectsNullScenario() {
        ScenarioOptions options = new ScenarioOptions(null, null, null, null, null, null, null);
        Scenario emptyScenario = null;
        ScenarioWrapper badPayload = new ScenarioWrapper(emptyScenario, options);

        ResponseEntity<AnalysisResponse> response = controller.analyze(badPayload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(ResponseStatus.INPUT_ERROR);
        verifyNoInteractions(sqc);
    }

    @Test
    void jsonRejectsNullRootStep() {
        ScenarioOptions options = new ScenarioOptions(null, null, null, null, null, null, null);
        Scenario scenario = new Scenario(null, null, null, null);
        ScenarioWrapper badPayload = new ScenarioWrapper(scenario, options);

        ResponseEntity<AnalysisResponse> response = controller.analyze(badPayload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(ResponseStatus.INPUT_ERROR);
        verifyNoInteractions(sqc);
    }
}
