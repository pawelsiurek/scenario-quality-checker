package pl.put.poznan.sqc.logic;

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
        return new ScenarioOptions(false, false, false, false, true, maxDepth);
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
}
