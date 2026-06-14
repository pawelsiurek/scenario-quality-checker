package pl.put.poznan.sqc.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.put.poznan.sqc.model.AnalysisResponse;
import pl.put.poznan.sqc.model.Scenario;
import pl.put.poznan.sqc.model.ScenarioOptions;
import pl.put.poznan.sqc.model.ScenarioWrapper;
import pl.put.poznan.sqc.model.Step;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SQCTest {

    private final SQC sqc = new SQC();

    @Test
    void deserializesScenarioOptionsFromJsonRequest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        ScenarioWrapper wrapper = objectMapper.readValue("""
                {
                  "scenario": {
                    "title": "Book addition",
                    "externalActors": ["Librarian"],
                    "systemActors": ["System"],
                    "rootStep": { "subSteps": [] }
                  },
                  "options": {
                    "includeTotalStepCount": false,
                    "includeKeywordStepCount": false,
                    "includeStepsWithoutActors": false,
                    "includeNumberedScenario": false,
                    "includeLimitedScenario": true,
                    "includeInvalidSteps": false,
                    "maxDepth": 2
                  }
                }
                """, ScenarioWrapper.class);

        assertThat(wrapper.options().includeTotalStepCount()).isFalse();
        assertThat(wrapper.options().includeKeywordStepCount()).isFalse();
        assertThat(wrapper.options().includeStepsWithoutActors()).isFalse();
        assertThat(wrapper.options().includeNumberedScenario()).isFalse();
        assertThat(wrapper.options().includeLimitedScenario()).isTrue();
        assertThat(wrapper.options().includeInvalidSteps()).isFalse();
        assertThat(wrapper.options().maxDepth()).isEqualTo(2);
    }

    @Test
    void returnsVisitorBasedAnalysisResults() {
        ScenarioWrapper wrapper = new ScenarioWrapper(
                sampleScenario(),
                new ScenarioOptions(true, true, true, true, false, false, 0)
        );

        AnalysisResponse response = sqc.analyze(wrapper);

        assertThat(response.getTotalStepCount()).isEqualTo(3);
        assertThat(response.getKeywordStepCount()).isEqualTo(1);
        assertThat(response.getStepsWithoutActors()).isNull();
        assertThat(response.getTextualScenario()).containsExactly(
                "1. Librarian starts scenario",
                "  1.1. IF: Librarian chooses copies",
                "    1.1.1. System confirms copy"
        );
    }

    @Test
    void returnsOnlyTopLevelStepsForDepthOne() {
        ScenarioWrapper wrapper = new ScenarioWrapper(
                sampleScenario(),
                limitedScenarioOptions(1)
        );

        AnalysisResponse response = sqc.analyze(wrapper);

        Step topLevelStep = response.getLimitedScenario().rootStep().getSubSteps().get(0);
        assertThat(response.getLimitedScenario().rootStep().getSubSteps()).hasSize(1);
        assertThat(topLevelStep.getText()).isEqualTo("Librarian starts scenario");
        assertThat(topLevelStep.getSubSteps()).isEmpty();
    }

    @Test
    void returnsSubScenariosOnlyUpToRequestedDepth() {
        ScenarioWrapper wrapper = new ScenarioWrapper(
                sampleScenario(),
                limitedScenarioOptions(2)
        );

        AnalysisResponse response = sqc.analyze(wrapper);

        Step topLevelStep = response.getLimitedScenario().rootStep().getSubSteps().get(0);
        Step directSubScenario = topLevelStep.getSubSteps().get(0);
        assertThat(topLevelStep.getSubSteps()).hasSize(1);
        assertThat(directSubScenario.getText()).isEqualTo("IF: Librarian chooses copies");
        assertThat(directSubScenario.getSubSteps()).isEmpty();
    }

    private ScenarioOptions limitedScenarioOptions(int maxDepth) {
        return new ScenarioOptions(false, false, false, false, true, false, maxDepth);
    }

    private Scenario sampleScenario() {
        Step deepestStep = step("System confirms copy", List.of());
        Step directSubScenario = step("IF: Librarian chooses copies", List.of(deepestStep));
        Step topLevelStep = step("Librarian starts scenario", List.of(directSubScenario));
        Step rootStep = step(null, List.of(topLevelStep));

        return new Scenario(
                "Book addition",
                List.of("Librarian"),
                List.of("System"),
                rootStep
        );
    }

    private Step step(String text, List<Step> subSteps) {
        Step step = new Step();
        step.setText(text);
        step.setSubSteps(subSteps);
        return step;
    }

    @Test
    void returnsNoDepthLimitWhenMaxDepthIsZero() {
        ScenarioWrapper wrapper = new ScenarioWrapper(
                sampleScenario(),
                new ScenarioOptions(false, false, false, false, true, false, 0)
        );

        AnalysisResponse response = sqc.analyze(wrapper);

        Step topLevelStep = response.getLimitedScenario().rootStep().getSubSteps().get(0);
        Step directSubScenario = topLevelStep.getSubSteps().get(0);
        Step deepestStep = directSubScenario.getSubSteps().get(0);
        
        assertThat(topLevelStep.getText()).isEqualTo("Librarian starts scenario");
        assertThat(directSubScenario.getText()).isEqualTo("IF: Librarian chooses copies");
        assertThat(deepestStep.getText()).isEqualTo("System confirms copy");
    }

    @Test
    void includesKeywordAndActorInTextualScenario() {
        ScenarioWrapper wrapper = new ScenarioWrapper(
                sampleScenario(),
                new ScenarioOptions(false, false, false, true, false, false, 0)
        );

        AnalysisResponse response = sqc.analyze(wrapper);

        assertThat(response.getTextualScenario()).isNotNull();
        assertThat(response.getTextualScenario()).isNotEmpty();
        assertThat(response.getTextualScenario().get(0)).contains("Librarian");
    }

    @Test
    void countStepsExcludesRootStep() {
        ScenarioWrapper wrapper = new ScenarioWrapper(
                sampleScenario(),
                new ScenarioOptions(true, false, false, false, false, false, 0)
        );

        AnalysisResponse response = sqc.analyze(wrapper);

        assertThat(response.getTotalStepCount()).isEqualTo(3);
        assertThat(response.getTotalStepCount()).isGreaterThan(0);
    }
}
