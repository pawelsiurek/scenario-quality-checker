package pl.put.poznan.sqc.logic.visitor;

import pl.put.poznan.sqc.model.Step;
import pl.put.poznan.sqc.model.StepVisitor;

/**
 * Counts all real scenario steps, excluding the invisible root step.
 */
public class TotalStepCountVisitor implements StepVisitor {
    private int count;

    /**
     * Creates a visitor with an initial count of zero.
     */
    public TotalStepCountVisitor() {}

    @Override
    public void visit(Step step, Step parentStep, int depth) {
        if (depth > 0) {
            count++;
        }
    }

    /**
     * Gets the number of visited scenario steps.
     *
     * @return total step count
     */
    public int getCount() {
        return count;
    }
}
