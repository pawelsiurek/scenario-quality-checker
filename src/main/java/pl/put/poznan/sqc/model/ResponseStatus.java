package pl.put.poznan.sqc.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResponseStatus {
    SUCCESS("success"),
    INPUT_ERROR("input_error"),
    SERVER_ERROR("server_error");

    private final String value;

    ResponseStatus(String value) {
        this.value = value;
    }

    // string to put in the JSON
    @JsonValue
    public String getValue() {
        return value;
    }
}