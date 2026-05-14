package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResponseStatus {
    SUCCESS("success"),
    ERROR("error");

    private final String value;

    ResponseStatus(String value) {
        this.value = value;
    }

    // Jackson will call this method to figure out what string to put in the JSON
    @JsonValue
    public String getValue() {
        return value;
    }
}