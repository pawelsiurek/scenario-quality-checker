package pl.put.poznan.sqc.rest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.put.poznan.sqc.logic.SQC;
import pl.put.poznan.sqc.model.AnalysisResponse;
import pl.put.poznan.sqc.model.ResponseStatus;
import pl.put.poznan.sqc.model.ScenarioWrapper;

/**
 * REST API Controller for Scenario Quality Checker analysis endpoints.
 * 
 * Provides HTTP endpoints for analyzing functional requirement scenarios in scenario format.
 * Delegates business logic to the {@link SQC} service and returns structured analysis results.
 * 
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/analyze - Analyze scenario with JSON response</li>
 *   <li>POST /api/analyze/text - Analyze scenario and return as formatted text file</li>
 * </ul>
 * 
 * @author Scenario Quality Checker Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/analyze")
public class SQCController {

    /** The SQC service instance for performing scenario analysis. */
    private final SQC sqc;

    /**
     * Constructs the controller with the SQC service dependency.
     * 
     * @param sqc the {@link SQC} service instance
     */
    public SQCController(SQC sqc) {
        this.sqc = sqc;
    }

    /**
     * Analyzes a scenario and returns comprehensive analysis results as JSON.
     * 
     * <p>Validates the input scenario, delegates to SQC service for analysis, and returns
     * results with appropriate HTTP status codes:
     * <ul>
     *   <li>200 OK - Analysis completed successfully</li>
     *   <li>400 Bad Request - Invalid input (missing scenario or root step)</li>
     *   <li>500 Internal Server Error - Server-side analysis error</li>
     * </ul>
     * 
     * @param scenarioWrapper the scenario and analysis options to process
     * @return ResponseEntity containing AnalysisResponse with status, metrics, and warnings
     */
    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(
            @RequestBody ScenarioWrapper scenarioWrapper
    ) {
        if (scenarioWrapper == null || scenarioWrapper.scenario() == null || scenarioWrapper.scenario().rootStep() == null) {
            return ResponseEntity.badRequest().body(
                    AnalysisResponse.builder().status(ResponseStatus.INPUT_ERROR).message("No scenario provided in the request body or no rootStep").build()
            );
        }

        AnalysisResponse result = sqc.analyze(scenarioWrapper);
        return switch(result.getStatus()) {
            case SUCCESS -> ResponseEntity.ok(result);
            case INPUT_ERROR -> ResponseEntity.badRequest().body(result);
            case SERVER_ERROR -> ResponseEntity.internalServerError().body(result);
        };
    }

    /**
     * Analyzes a scenario and returns the result as a formatted text file.
     * 
     * <p>Generates a numbered and indented textual representation of the scenario
     * suitable for download as a text file. Returns 400 Bad Request if analysis fails
     * or input is invalid.
     * 
     * @param scenarioWrapper the scenario and analysis options to process
     * @return ResponseEntity containing the scenario as plain text with appropriate filename header
     */
    @PostMapping("/text")
    public ResponseEntity<String> analyzeText(
            @RequestBody ScenarioWrapper scenarioWrapper
    ) {
        if (scenarioWrapper == null || scenarioWrapper.scenario() == null || scenarioWrapper.scenario().rootStep() == null) {
            return ResponseEntity.badRequest().body("No scenario provided in the request body or no rootStep");
        }

        String textScenario = sqc.analyzeToText(scenarioWrapper);
        if (textScenario == null || textScenario.isEmpty()) {
            return ResponseEntity.badRequest().body("Failed to generate text scenario");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"scenario.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(textScenario);
    }
}
