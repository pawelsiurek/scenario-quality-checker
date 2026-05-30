package pl.put.poznan.sqc.logic;

import org.junit.jupiter.api.Test;
import pl.put.poznan.sqc.model.ScenarioOptions;

import static org.assertj.core.api.Assertions.assertThat;

public class ScenarioOptionsTest {
    @Test
    void defaultOptions() {
        ScenarioOptions options = new ScenarioOptions(null, null, null, null, null, null, null);
        assertThat(options.includeTotalStepCount()).isTrue();
        assertThat(options.includeKeywordStepCount()).isTrue();
        assertThat(options.includeStepsWithoutActors()).isTrue();
        assertThat(options.includeNumberedScenario()).isTrue();
        assertThat(options.includeLimitedScenario()).isTrue();
        assertThat(options.includeInvalidSteps()).isTrue();
        assertThat(options.maxDepth()).isEqualTo(0);
    }

    @Test
    void someOptionsSet() {
        ScenarioOptions options = new ScenarioOptions(
                false,
                null,
                false,
                null,
                true,
                null,
                5
        );
        assertThat(options.includeTotalStepCount()).isFalse();
        assertThat(options.includeKeywordStepCount()).isTrue();
        assertThat(options.includeStepsWithoutActors()).isFalse();
        assertThat(options.includeNumberedScenario()).isTrue();
        assertThat(options.includeLimitedScenario()).isTrue();
        assertThat(options.includeInvalidSteps()).isTrue();
        assertThat(options.maxDepth()).isEqualTo(5);
    }
}
