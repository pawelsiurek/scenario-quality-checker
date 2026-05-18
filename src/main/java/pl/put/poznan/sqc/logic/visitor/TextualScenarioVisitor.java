package pl.put.poznan.sqc.logic.visitor;

import pl.put.poznan.sqc.model.Step;
import pl.put.poznan.sqc.model.StepVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a numbered text representation of a scenario tree.
 */
public class TextualScenarioVisitor implements StepVisitor {
    private final int maxDepth;
    private final List<String> textualScenario = new ArrayList<>();

    /**
     * Creates a visitor for a selected maximum nesting depth.
     *
     * @param maxDepth maximum depth, where 0 means unlimited
     */
    public TextualScenarioVisitor(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public void visit(Step step, Step parentStep, int depth) {
        if (depth == 0 || (maxDepth > 0 && depth > maxDepth)) {
            return;
        }

        String indentation = "  ".repeat(depth - 1);
        textualScenario.add(indentation + step.getOrderNumber() + " " + buildStepText(step));
    }

    /**
     * Gets the generated textual scenario.
     *
     * @return numbered scenario lines, or null when no lines were generated
     */
    public List<String> getTextualScenario() {
        return textualScenario.isEmpty() ? null : textualScenario;
    }

    private String buildStepText(Step step) {
        StringBuilder sb = new StringBuilder();

        if (step.getKeyword() != null) {
            sb.append(step.getKeyword()).append(": ");
        }

        if (step.getActor() != null) {
            sb.append(step.getActor()).append(" ");
        }

        if (step.getCleanText() != null) {
            sb.append(step.getCleanText());
        }

        return sb.toString().trim();
    }
}
