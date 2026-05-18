package pl.put.poznan.sqc.rest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.put.poznan.sqc.logic.SQC;
import pl.put.poznan.sqc.model.AnalysisResponse;
import pl.put.poznan.sqc.model.ResponseStatus;
import pl.put.poznan.sqc.model.ScenarioWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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

    private static final Logger logger = LoggerFactory.getLogger(SQCController.class);

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
    @Operation(
            summary = "Analyze a scenario",
            description = "Validates the scenario tree, identifies missing actors, calculates step metrics, and generates a formatted text output."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "The scenario wrapper containing the tree and configuration options.",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Comprehensive Example",
                            summary = "A standard scenario with nested IF statements",
                            value = """
                            {
                              "scenario": {
                                "title": "Adding a new book to the catalog",
                                "externalActors": ["Librarian"],
                                "systemActors": ["System", "Database"],
                                "rootStep": {
                                  "subSteps": [
                                    { "text": "Librarian enters book details" },
                                    {
                                      "text": "IF: System validates the ISBN",
                                      "subSteps": [
                                        { "text": "Database saves the record" },
                                        {
                                          "text": "System displays success message",
                                          "subSteps": [
                                            { "text": "This step is too deep and will be ignored by maxDepth!" }
                                          ]
                                        }
                                      ]
                                    },
                                    { "text": "Generates an error because there is no actor here" }
                                  ]
                                }
                              }
                            }
                            """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Analysis completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                    {
                                         "status": "success",
                                         "message": "Analysis successfully completed",
                                         "warnings": [
                                           "Step 2.2. has no keyword, but has children"
                                         ],
                                         "totalStepCount": 6,
                                         "keywordStepCount": 1,
                                         "stepsWithoutActors": [
                                           "2.2.1.",
                                           "3."
                                         ],
                                         "textualScenario": [
                                           "1. Librarian enters book details",
                                           "2. IF: System validates the ISBN",
                                           "  2.1. Database saves the record",
                                           "  2.2. System displays success message",
                                           "    2.2.1. This step is too deep and will be ignored by maxDepth!",
                                           "3. Generates an error because there is no actor here"
                                         ]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input (e.g., missing root step or actors)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "status": "input_error",
                                      "message": "No scenario provided in the request body or no rootStep"
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(
            @RequestBody ScenarioWrapper scenarioWrapper
    ) {
        logger.info("Received scenario analysis request");
        logger.debug("Scenario analysis request body: {}", scenarioWrapper);

        if (scenarioWrapper == null || scenarioWrapper.scenario() == null || scenarioWrapper.scenario().rootStep() == null) {
            logger.info("Rejected scenario analysis request due to missing scenario or root step");
            return ResponseEntity.badRequest().body(
                    AnalysisResponse.builder().status(ResponseStatus.INPUT_ERROR).message("No scenario provided in the request body or no rootStep").build()
            );
        }

        AnalysisResponse result = sqc.analyze(scenarioWrapper);
        logger.info("Scenario analysis completed with status {}", result.getStatus());
        logger.debug("Scenario analysis response: {}", result);
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
        logger.info("Received text scenario analysis request");
        logger.debug("Text scenario analysis request body: {}", scenarioWrapper);

        if (scenarioWrapper == null || scenarioWrapper.scenario() == null || scenarioWrapper.scenario().rootStep() == null) {
            logger.info("Rejected text scenario analysis request due to missing scenario or root step");
            return ResponseEntity.badRequest().body("No scenario provided in the request body or no rootStep");
        }

        String textScenario = sqc.analyzeToText(scenarioWrapper);
        if (textScenario == null || textScenario.isEmpty()) {
            logger.info("Text scenario generation failed");
            return ResponseEntity.badRequest().body("Failed to generate text scenario");
        }

        logger.info("Text scenario generated successfully");
        logger.debug("Generated text scenario: {}", textScenario);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"scenario.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(textScenario);
    }
}
