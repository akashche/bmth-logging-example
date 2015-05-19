package com.redhat.byteman.thermostat.frontend;

/**
 * User: alexkasko
 * Date: 5/19/15
 */
public class FrontendException extends RuntimeException {
    public FrontendException(String message) {
        super(message);
    }

    public FrontendException(String message, Throwable cause) {
        super(message, cause);
    }
}
