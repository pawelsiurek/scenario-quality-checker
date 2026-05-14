package pl.put.poznan.sqc.rest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.put.poznan.sqc.model.AnalysisResponse;
import pl.put.poznan.sqc.model.ScenarioWrapper;


@RestController
@RequestMapping("/api/analyze")
public class SQCController {

    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(
            @RequestBody ScenarioWrapper scenarioWrapper
    ) {
        AnalysisResponse result = new AnalysisResponse(10, scenarioWrapper.scenario().text());
        return ResponseEntity.ok(result);
    }
}


