package pl.put.poznan.sqc.model;

public record ScenarioWrapper (
        Scenario scenario,
        ScenarioOptions options
) {
    public ScenarioWrapper {
        // scenario demanded to be non null
        // if options are null, result to defaults
        if (options == null) {
            options = new ScenarioOptions(null, null, null, null, null, null, null);
        }
    }
}
