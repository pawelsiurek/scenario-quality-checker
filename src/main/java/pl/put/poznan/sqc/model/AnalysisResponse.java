package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnalysisResponse(
        ResponseStatus status,
        String message,
        Integer totalStepCount,
        Integer keywordStepCount,
        // Decide how to represent it, probably full line, with its number, would require the next one
        List<String> stepsWithoutActors,
        // Decide how to represent it, probably again as a nested json; take account for max depth
        List<String> numberedScenario,
        List<String> otherMistakes,
        List<String> warnings
) {
}
