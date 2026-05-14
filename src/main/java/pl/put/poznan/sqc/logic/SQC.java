package pl.put.poznan.sqc.logic;

import pl.put.poznan.sqc.model.AnalysisResponse;
import pl.put.poznan.sqc.model.ResponseStatus;
import pl.put.poznan.sqc.model.ScenarioWrapper;

import static java.awt.SystemColor.text;

public class SQC {
    public SQC(){

    }

    public AnalysisResponse analyze (ScenarioWrapper scenarioWrapper){
        // First, normalise the parsed json. Specifically, separate textual steps into: keyword, actor and cleanText. No value: null. Mark any errors at this stage
        // Only the root step is allowed to have no text (shouldnt, but maybe it can)
        // I think that it should be allowed for a step with keyword to have no children
        // Check for errors like ELSE without IF

        // Second, perform computations according to flags. If flag is set to false, return null on place of answer

        return new AnalysisResponse(
                ResponseStatus.SUCCESS,
                "example success message",
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
