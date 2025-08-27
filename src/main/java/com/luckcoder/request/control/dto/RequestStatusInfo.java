package com.luckcoder.request.control.dto;

import java.time.Instant;

public class RequestStatusInfo {
    
    private boolean enabled;
    private Instant timestamp;
    private String operator;
    
    public RequestStatusInfo() {
    }
    
    public RequestStatusInfo(boolean enabled, Instant timestamp, String operator) {
        this.enabled = enabled;
        this.timestamp = timestamp;
        this.operator = operator;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}