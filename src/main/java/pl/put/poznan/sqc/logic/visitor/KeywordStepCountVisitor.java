package pl.put.poznan.sqc.logic.visitor;

import pl.put.poznan.sqc.model.Step;
import pl.put.poznan.sqc.model.StepVisitor;

/**
 * Counts scenario steps that contain a recognized keyword.
 */
public class KeywordStepCountVisitor implements StepVisitor {
    private int count;

    /**
     * Creates a visitor with an initial count of zero.
     */
    public KeywordStepCountVisitor() {}

    @Override
    public void visit(Step step, Step parentStep, int depth) {
        if (depth > 0 && step.getKeyword() != null) {
            count++;
        }
    }

    /**
     * Gets the number of keyword steps.
     *
     * @return keyword step count
     */
    public int getCount() {
        return count;
    }
}
