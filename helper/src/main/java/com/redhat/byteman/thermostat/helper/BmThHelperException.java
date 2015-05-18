package com.redhat.byteman.thermostat.helper;

/**
 * Module-specific exception
 *
 * User: alexkasko
 * Date: 5/18/15
 */
public class BmThHelperException extends RuntimeException {
    public BmThHelperException(String message) {
        super(message);
    }

    public BmThHelperException(String message, Throwable cause) {
        super(message, cause);
    }
}
