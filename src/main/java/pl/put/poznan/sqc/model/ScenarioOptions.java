package pl.put.poznan.sqc.model;

public record ScenarioOptions (
        // All true by default
        Boolean includeTotalStepCount,
        Boolean includeKeywordStepCount,
        Boolean includeStepsWithoutActors,
        Boolean includeNumberedScenario,
        Boolean includeLimitedScenario,
        Integer maxDepth
) {
    public ScenarioOptions {
        if(includeTotalStepCount == null) includeTotalStepCount = true;
        if(includeKeywordStepCount == null) includeKeywordStepCount = true;
        if(includeStepsWithoutActors == null) includeStepsWithoutActors = true;
        if(includeNumberedScenario == null) includeNumberedScenario = true;
        if(includeLimitedScenario == null) includeLimitedScenario = true;
        if(maxDepth == null) maxDepth = 0;
    }
}
