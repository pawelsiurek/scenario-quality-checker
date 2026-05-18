package pl.put.poznan.sqc.model;

/**
 * Visits steps while traversing a scenario tree.
 */
@FunctionalInterface
public interface StepVisitor {
    /**
     * Handles one step during traversal.
     *
     * @param step current step
     * @param parentStep parent step, or null for the root
     * @param depth nesting depth, where root has depth 0
     */
    void visit(Step step, Step parentStep, int depth);
}
