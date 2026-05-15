package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Step {
    private String text;
    private List<Step> subSteps = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private String keyword;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private String actor;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private String cleanText;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) private String orderNumber = "";

    public Step() {}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null || text.isBlank() ? null : text.trim();
    }

    public List<Step> getSubSteps() {
        return subSteps;
    }

    public void setSubSteps(List<Step> subSteps) {
        if(subSteps == null) {
            subSteps = List.of();
        }
        this.subSteps = subSteps;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor == null || actor.isBlank() ? null : actor.trim();
    }

    public String getCleanText() {
        return cleanText;
    }

    public void setCleanText(String cleanText) {
        this.cleanText = cleanText == null || cleanText.isBlank() ? null : cleanText.trim();
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber == null || orderNumber.isBlank() ? "" : orderNumber.trim();
    }
}
