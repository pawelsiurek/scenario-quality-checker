package pl.put.poznan.sqc.rest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.put.poznan.sqc.logic.SQC;
import pl.put.poznan.sqc.model.AnalysisResponse;
import pl.put.poznan.sqc.model.ResponseStatus;
import pl.put.poznan.sqc.model.ScenarioWrapper;


@RestController
@RequestMapping("/api/analyze")
public class SQCController {

    private final SQC sqc = new SQC();

    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(
            @RequestBody ScenarioWrapper scenarioWrapper
    ) {
        if (scenarioWrapper == null || scenarioWrapper.scenario() == null) {
            AnalysisResponse errorResponse = new AnalysisResponse(
                    ResponseStatus.ERROR,
                    "No scenario provided in the request body.",
                    null, null, null, null, null, null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        AnalysisResponse result = sqc.analyze(scenarioWrapper);
        return ResponseEntity.ok(result);
    }
}


