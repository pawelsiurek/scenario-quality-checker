package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single step in a scenario.
 * 
 * A step is a unit of action within a scenario, potentially containing nested sub-steps.
 * Steps can include flow control keywords (IF, ELSE, FOR EACH) and identify the actor
 * performing the action. Text content is automatically trimmed and normalized.
 * 
 * <p>Field responsibilities:
 * <ul>
 *   <li>{@code text} - User-provided raw step description</li>
 *   <li>{@code subSteps} - Nested child steps for composite scenarios</li>
 *   <li>{@code keyword} - Extracted flow control keyword (IF, ELSE, FOR EACH) - read-only</li>
 *   <li>{@code actor} - Extracted actor performing the step - read-only</li>
 *   <li>{@code cleanText} - Text content after removing keyword and actor - read-only</li>
 *   <li>{@code orderNumber} - Hierarchical numbering (e.g., "1.2.3.") - read-only</li>
 * </ul>
 * 
 * @author Scenario Quality Checker Team
 * @version 1.0
 */
public class Step {
    /** Raw step text content provided by the user. Automatically trimmed. */
    private String text;
    
    /** List of nested sub-steps. Empty list by default. */
    private List<Step> subSteps = new ArrayList<>();

    /** Flow control keyword extracted from the step text (IF, ELSE, FOR EACH). Read-only, derived from text. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private String keyword;
    
    /** Actor name extracted from the step text (e.g., "Librarian", "System"). Read-only, derived from text. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private String actor;
    
    /** Step text content after removing keyword and actor prefix. Read-only, derived from text. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private String cleanText;
    
    /** Hierarchical step numbering (e.g., "1.2.3."). Read-only, assigned during normalization. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private String orderNumber = "";

    /**
     * Constructs an empty Step with no text or substeps.
     */
    public Step() {}

    /**
     * Gets the raw step text content.
     * 
     * @return the step text, or null if not set
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the step text content.
     * Automatically trims whitespace and converts blank strings to null.
     * 
     * @param text the step description text
     */
    public void setText(String text) {
        this.text = text == null || text.isBlank() ? null : text.trim();
    }

    /**
     * Gets the list of nested sub-steps.
     * 
     * @return the list of sub-steps, or empty list if none defined
     */
    public List<Step> getSubSteps() {
        return subSteps;
    }

    /**
     * Sets the list of nested sub-steps.
     * If null is provided, an empty list is assigned.
     * 
     * @param subSteps the list of child steps
     */
    public void setSubSteps(List<Step> subSteps) {
        if(subSteps == null) {
            subSteps = List.of();
        }
        this.subSteps = subSteps;
    }

    /**
     * Gets the flow control keyword (IF, ELSE, FOR EACH).
     * This is a read-only field extracted during analysis.
     * 
     * @return the keyword if present, null otherwise
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Sets the flow control keyword.
     * Automatically trims whitespace and converts blank strings to null.
     * 
     * @param keyword the flow control keyword
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    /**
     * Gets the actor performing this step.
     * This is a read-only field extracted during analysis.
     * 
     * @return the actor name if present, null otherwise
     */
    public String getActor() {
        return actor;
    }

    /**
     * Sets the actor performing this step.
     * Automatically trims whitespace and converts blank strings to null.
     * 
     * @param actor the actor name
     */
    public void setActor(String actor) {
        this.actor = actor == null || actor.isBlank() ? null : actor.trim();
    }

    /**
     * Gets the clean step text with keyword and actor removed.
     * This is a read-only field extracted during analysis.
     * 
     * @return the text content after removing prefix elements, or null if empty
     */
    public String getCleanText() {
        return cleanText;
    }

    /**
     * Sets the clean step text.
     * Automatically trims whitespace and converts blank strings to null.
     * 
     * @param cleanText the text without keyword/actor prefix
     */
    public void setCleanText(String cleanText) {
        this.cleanText = cleanText == null || cleanText.isBlank() ? null : cleanText.trim();
    }

    /**
     * Gets the hierarchical order number (e.g., "1.2.3.").
     * This is a read-only field assigned during normalization.
     * 
     * @return the order number string
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    /**
     * Sets the hierarchical order number.
     * Automatically trims whitespace. Blank strings result in empty string (not null).
     * 
     * @param orderNumber the step numbering in hierarchy
     */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber == null || orderNumber.isBlank() ? "" : orderNumber.trim();
    }

    /**
     * Accepts a visitor and traverses this step with all nested sub-steps.
     *
     * @param visitor visitor to execute for each step
     */
    public void accept(StepVisitor visitor) {
        accept(visitor, null, 0);
    }

    private void accept(StepVisitor visitor, Step parentStep, int depth) {
        visitor.visit(this, parentStep, depth);
        for (Step subStep : subSteps) {
            subStep.accept(visitor, this, depth + 1);
        }
    }
}
