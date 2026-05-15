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

    private final SQC sqc;

    public SQCController(SQC sqc) {
        this.sqc = sqc;
    }

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
}


