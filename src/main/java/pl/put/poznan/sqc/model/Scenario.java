package pl.put.poznan.sqc.model;

import java.util.List;

public record Scenario (
    String title,
    List<String> externalActors,
    List<String> systemActors,
    Step rootStep
) {
    public Scenario {
        title = title == null || title.isBlank() ? null : title.trim();
        externalActors = externalActors == null ? List.of() : externalActors;
        systemActors = systemActors == null ? List.of() : systemActors;
    }
}
