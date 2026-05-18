package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ScenarioOptions (
        // All true by default
        @JsonProperty("includeTotalStepCount")
        Boolean includeTotalStepCount,
        @JsonProperty("includeKeywordStepCount")
        Boolean includeKeywordStepCount,
        @JsonProperty("includeStepsWithoutActors")
        Boolean includeStepsWithoutActors,
        @JsonProperty("includeNumberedScenario")
        Boolean includeNumberedScenario,
        @JsonProperty("includeLimitedScenario")
        Boolean includeLimitedScenario,
        @JsonProperty("includeInvalidSteps")
        Boolean includeInvalidSteps,
        @JsonProperty("maxDepth")
        Integer maxDepth
) {
    public ScenarioOptions {
        if(includeTotalStepCount == null) includeTotalStepCount = true;
        if(includeKeywordStepCount == null) includeKeywordStepCount = true;
        if(includeStepsWithoutActors == null) includeStepsWithoutActors = true;
        if(includeNumberedScenario == null) includeNumberedScenario = true;
        if(includeLimitedScenario == null) includeLimitedScenario = true;
        if(includeInvalidSteps == null) includeInvalidSteps = true;
        if(maxDepth == null) maxDepth = 0;
    }
}
