package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Step(
    String text,
    List<Step> subSteps,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY) String keyword,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) String actor,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) String cleanText
) {
    public Step {
        if(subSteps == null) {
            subSteps = List.of();
        };
    }
}
