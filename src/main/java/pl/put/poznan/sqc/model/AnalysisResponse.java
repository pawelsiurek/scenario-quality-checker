package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AnalysisResponse {

    private final ResponseStatus status;
    private final String message;
    private final List<String> warnings;
    private final Integer totalStepCount;
    private final Integer keywordStepCount;
    private final List<String> stepsWithoutActors;
    private final List<String> invalidSteps;
    private final List<String> textualScenario;
    private final Scenario limitedScenario;
    private final List<String> otherMistakes;

    public AnalysisResponse() {
        this(new Builder());
    }

    private AnalysisResponse(Builder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.warnings = builder.warnings;
        this.totalStepCount = builder.totalStepCount;
        this.keywordStepCount = builder.keywordStepCount;
        this.stepsWithoutActors = builder.stepsWithoutActors;
        this.invalidSteps = builder.invalidSteps;
        this.textualScenario = builder.textualScenario;
        this.limitedScenario = builder.limitedScenario;
        this.otherMistakes = builder.otherMistakes;
    }

    public ResponseStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public List<String> getWarnings() { return warnings; }
    public Integer getTotalStepCount() { return totalStepCount; }
    public Integer getKeywordStepCount() { return keywordStepCount; }
    public List<String> getStepsWithoutActors() { return stepsWithoutActors; }
    public List<String> getInvalidSteps() { return invalidSteps; }
    public List<String> getTextualScenario() { return textualScenario; }
    public Scenario getLimitedScenario() { return limitedScenario; }
    public List<String> getOtherMistakes() { return otherMistakes; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ResponseStatus status = ResponseStatus.SUCCESS;
        private String message;
        private List<String> warnings;
        private Integer totalStepCount;
        private Integer keywordStepCount;
        private List<String> stepsWithoutActors;
        private List<String> invalidSteps;
        private List<String> textualScenario;
        private Scenario limitedScenario;
        private List<String> otherMistakes;

        private Builder() {}

        public Builder status(ResponseStatus status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public Builder totalStepCount(Integer totalStepCount) {
            this.totalStepCount = totalStepCount;
            return this;
        }

        public Builder keywordStepCount(Integer keywordStepCount) {
            this.keywordStepCount = keywordStepCount;
            return this;
        }

        public Builder stepsWithoutActors(List<String> stepsWithoutActors) {
            this.stepsWithoutActors = stepsWithoutActors;
            return this;
        }

        public Builder invalidSteps(List<String> invalidSteps) {
            this.invalidSteps = invalidSteps;
            return this;
        }

        public Builder textualScenario(List<String> textualScenario) {
            this.textualScenario = textualScenario;
            return this;
        }

        public Builder limitedScenario(Scenario limitedScenario) {
            this.limitedScenario = limitedScenario;
            return this;
        }

        public Builder otherMistakes(List<String> otherMistakes) {
            this.otherMistakes = otherMistakes;
            return this;
        }

        public AnalysisResponse build() {
            return new AnalysisResponse(this);
        }
    }
}
