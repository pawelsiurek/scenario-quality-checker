package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Encapsulates the complete analysis results of a scenario.
 * 
 * This immutable response object contains analysis status, metrics, findings, and a reconstructed
 * scenario representation. Built using the Builder pattern with flexible field inclusion via
 * {@link JsonInclude} annotation (empty lists/nulls are excluded from JSON serialization).
 * 
 * <p>Field responsibilities:
 * <ul>
 *   <li>{@code status} - Overall analysis status (SUCCESS, INPUT_ERROR, SERVER_ERROR)</li>
 *   <li>{@code message} - Human-readable status message</li>
 *   <li>{@code warnings} - List of warnings detected during normalization and analysis</li>
 *   <li>{@code totalStepCount} - Count of all steps in the scenario</li>
 *   <li>{@code keywordStepCount} - Count of steps with flow control keywords</li>
 *   <li>{@code stepsWithoutActors} - Step order numbers lacking actor assignment</li>
 *   <li>{@code invalidSteps} - Step order numbers with quality issues</li>
 *   <li>{@code textualScenario} - Numbered and formatted scenario as string list</li>
 *   <li>{@code limitedScenario} - Scenario restricted to specified nesting depth</li>
 *   <li>{@code otherMistakes} - Additional quality issues detected</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>
 * AnalysisResponse response = AnalysisResponse.builder()
 *     .status(ResponseStatus.SUCCESS)
 *     .message("Analysis completed")
 *     .totalStepCount(5)
 *     .warnings(warnings)
 *     .build();
 * </pre>
 * 
 * @author Scenario Quality Checker Team
 * @version 1.0
 * @see ResponseStatus
 * @see Builder
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AnalysisResponse {

    /** Overall status of the analysis result. */
    private final ResponseStatus status;
    
    /** Human-readable message describing the result or error. */
    private final String message;
    
    /** List of warnings generated during normalization and analysis. */
    private final List<String> warnings;
    
    /** Total count of all steps in the scenario (null if not requested). */
    private final Integer totalStepCount;
    
    /** Count of steps containing flow control keywords (null if not requested). */
    private final Integer keywordStepCount;
    
    /** Order numbers of steps without actor assignment (null if all have actors). */
    private final List<String> stepsWithoutActors;
    
    /** Order numbers of steps with detected quality issues (null if none found). */
    private final List<String> invalidSteps;
    
    /** Numbered, formatted scenario representation as list of strings (null if not requested). */
    private final List<String> textualScenario;
    
    /** Scenario limited to specified depth (null if not requested). */
    private final Scenario limitedScenario;
    
    /** Additional quality issues detected (reserved for future use). */
    private final List<String> otherMistakes;

    /**
     * Constructs an AnalysisResponse using the default builder.
     */
    public AnalysisResponse() {
        this(new Builder());
    }

    /**
     * Constructs an AnalysisResponse from a builder.
     * 
     * @param builder the builder containing the response data
     */
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

    /**
     * Gets the analysis status.
     * @return the response status
     */
    public ResponseStatus getStatus() { return status; }
    
    /**
     * Gets the status message.
     * @return the message string
     */
    public String getMessage() { return message; }
    
    /**
     * Gets the warnings list.
     * @return list of warnings, may be null or empty
     */
    public List<String> getWarnings() { return warnings; }
    
    /**
     * Gets the total step count.
     * @return total steps, or null if not computed
     */
    public Integer getTotalStepCount() { return totalStepCount; }
    
    /**
     * Gets the keyword step count.
     * @return count of steps with keywords, or null if not computed
     */
    public Integer getKeywordStepCount() { return keywordStepCount; }
    
    /**
     * Gets steps without actors.
     * @return order numbers of steps lacking actors, or null if all have actors
     */
    public List<String> getStepsWithoutActors() { return stepsWithoutActors; }
    
    /**
     * Gets invalid steps.
     * @return order numbers of invalid steps, or null if none found
     */
    public List<String> getInvalidSteps() { return invalidSteps; }
    
    /**
     * Gets the textual scenario.
     * @return numbered scenario as list of strings, or null if not generated
     */
    public List<String> getTextualScenario() { return textualScenario; }
    
    /**
     * Gets the depth-limited scenario.
     * @return scenario restricted to depth, or null if not generated
     */
    public Scenario getLimitedScenario() { return limitedScenario; }
    
    /**
     * Gets other detected mistakes.
     * @return list of other quality issues, or null if none found
     */
    public List<String> getOtherMistakes() { return otherMistakes; }

    /**
     * Creates a new builder for constructing AnalysisResponse instances.
     * 
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing AnalysisResponse objects with fluent API.
     * Allows flexible configuration of response fields with reasonable defaults.
     */
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

        /**
         * Sets the response status.
         * @param status the response status
         * @return this builder
         */
        public Builder status(ResponseStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the status message.
         * @param message the message string
         * @return this builder
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the warnings list.
         * @param warnings list of warning strings
         * @return this builder
         */
        public Builder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        /**
         * Sets the total step count.
         * @param totalStepCount number of steps
         * @return this builder
         */
        public Builder totalStepCount(Integer totalStepCount) {
            this.totalStepCount = totalStepCount;
            return this;
        }

        /**
         * Sets the keyword step count.
         * @param keywordStepCount count of keyword steps
         * @return this builder
         */
        public Builder keywordStepCount(Integer keywordStepCount) {
            this.keywordStepCount = keywordStepCount;
            return this;
        }

        /**
         * Sets the steps without actors.
         * @param stepsWithoutActors order numbers of steps lacking actors
         * @return this builder
         */
        public Builder stepsWithoutActors(List<String> stepsWithoutActors) {
            this.stepsWithoutActors = stepsWithoutActors;
            return this;
        }

        /**
         * Sets the invalid steps.
         * @param invalidSteps order numbers of invalid steps
         * @return this builder
         */
        public Builder invalidSteps(List<String> invalidSteps) {
            this.invalidSteps = invalidSteps;
            return this;
        }

        /**
         * Sets the textual scenario.
         * @param textualScenario scenario as list of formatted strings
         * @return this builder
         */
        public Builder textualScenario(List<String> textualScenario) {
            this.textualScenario = textualScenario;
            return this;
        }

        /**
         * Sets the limited scenario.
         * @param limitedScenario scenario restricted to depth
         * @return this builder
         */
        public Builder limitedScenario(Scenario limitedScenario) {
            this.limitedScenario = limitedScenario;
            return this;
        }

        /**
         * Sets other detected mistakes.
         * @param otherMistakes list of additional quality issues
         * @return this builder
         */
        public Builder otherMistakes(List<String> otherMistakes) {
            this.otherMistakes = otherMistakes;
            return this;
        }

        /**
         * Builds and returns the immutable AnalysisResponse.
         * 
         * @return a new AnalysisResponse instance
         */
        public AnalysisResponse build() {
            return new AnalysisResponse(this);
        }
    }
}
