package pl.put.poznan.sqc.logic.visitor;

import pl.put.poznan.sqc.model.Step;
import pl.put.poznan.sqc.model.StepVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects scenario steps that do not have an assigned actor.
 */
public class StepsWithoutActorsVisitor implements StepVisitor {
    private final List<String> stepsWithoutActors = new ArrayList<>();

    /**
     * Creates a visitor with an empty result list.
     */
    public StepsWithoutActorsVisitor() {}

    @Override
    public void visit(Step step, Step parentStep, int depth) {
        if (depth > 0 && step.getActor() == null) {
            stepsWithoutActors.add(step.getOrderNumber());
        }
    }

    /**
     * Gets order numbers of steps without actors.
     *
     * @return order numbers, or null when every step has an actor
     */
    public List<String> getStepsWithoutActors() {
        return stepsWithoutActors.isEmpty() ? null : stepsWithoutActors;
    }
}
