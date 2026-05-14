package pl.put.poznan.sqc.model;

import java.util.List;

public record Scenario (
    String title,
    List<String> actors,
    List<String> systemActors,
    Step rootStep
) {}
