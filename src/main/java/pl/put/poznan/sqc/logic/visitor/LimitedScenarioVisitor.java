package pl.put.poznan.sqc.logic.visitor;

import pl.put.poznan.sqc.model.Step;
import pl.put.poznan.sqc.model.StepVisitor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Creates a copy of a scenario tree limited to a selected depth.
 */
public class LimitedScenarioVisitor implements StepVisitor {
    private final int maxDepth;
    private final Map<Step, Step> copiedSteps = new IdentityHashMap<>();
    private Step rootStep;

    /**
     * Creates a visitor for a selected maximum nesting depth.
     *
     * @param maxDepth maximum depth, where 0 means unlimited
     */
    public LimitedScenarioVisitor(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public void visit(Step step, Step parentStep, int depth) {
        if (maxDepth > 0 && depth > maxDepth) {
            return;
        }

        Step copiedStep = copyStep(step);
        copiedSteps.put(step, copiedStep);

        if (parentStep == null) {
            rootStep = copiedStep;
            return;
        }

        Step copiedParent = copiedSteps.get(parentStep);
        if (copiedParent != null) {
            copiedParent.getSubSteps().add(copiedStep);
        }
    }

    /**
     * Gets the copied root step.
     *
     * @return copied root step
     */
    public Step getRootStep() {
        return rootStep;
    }

    private Step copyStep(Step sourceStep) {
        Step copiedStep = new Step();
        copiedStep.setText(sourceStep.getText());
        copiedStep.setKeyword(sourceStep.getKeyword());
        copiedStep.setActor(sourceStep.getActor());
        copiedStep.setCleanText(sourceStep.getCleanText());
        copiedStep.setOrderNumber(sourceStep.getOrderNumber());
        copiedStep.setSubSteps(new ArrayList<>());
        return copiedStep;
    }
}
