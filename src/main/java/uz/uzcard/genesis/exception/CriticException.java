package uz.uzcard.genesis.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class CriticException extends RuntimeException implements Serializable {

    @JsonIgnore
    public static String suppressed;

    public CriticException(String message) {
        super(message);
    }

    public CriticException(String message, Throwable cause) {
        super(message, cause);
    }

    public CriticException() {
    }

    @JsonIgnore
    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    @JsonIgnore
    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @JsonIgnore
    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

}