package pl.put.poznan.sqc.model;

import java.util.List;

/**
 * Represents a functional requirement scenario.
 * 
 * A scenario is a structured description of a system's behavior consisting of actors
 * and a sequence of steps. It follows the scenario format guidelines with a title,
 * external and system actors, and hierarchical step structure.
 * 
 * <p>Record fields:
 * <ul>
 *   <li>{@code title} - Name of the scenario</li>
 *   <li>{@code externalActors} - Users or external systems interacting with the system</li>
 *   <li>{@code systemActors} - System components or services involved</li>
 *   <li>{@code rootStep} - Invisible root step containing all top-level scenario steps</li>
 * </ul>
 * 
 * <p>Normalization rules:
 * <ul>
 *   <li>Title is trimmed; blank titles become null</li>
 *   <li>Actor lists are normalized to empty lists if null</li>
 *   <li>Root step is required and must be provided</li>
 * </ul>
 * 
 * @param title the scenario name
 * @param externalActors list of external actors (users, external systems)
 * @param systemActors list of system actors (components, services)
 * @param rootStep the invisible root step containing scenario steps
 * 
 * @author Scenario Quality Checker Team
 * @version 1.0
 */
public record Scenario (
    String title,
    List<String> externalActors,
    List<String> systemActors,
    Step rootStep
) {
    /**
     * Compact constructor that normalizes scenario data.
     * Trims title, converts null actor lists to empty lists.
     * 
     * @param title the scenario name
     * @param externalActors list of external actors
     * @param systemActors list of system actors
     * @param rootStep the invisible root step
     */
    public Scenario {
        title = title == null || title.isBlank() ? null : title.trim();
        externalActors = externalActors == null ? List.of() : externalActors;
        systemActors = systemActors == null ? List.of() : systemActors;
    }
}
