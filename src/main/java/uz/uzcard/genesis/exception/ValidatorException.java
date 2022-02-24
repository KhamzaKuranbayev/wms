package uz.uzcard.genesis.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import java.io.Serializable;

public class ValidatorException extends RuntimeException implements Serializable {

    @JsonIgnore
    public String suppressed;

    public ValidatorException(String message) {
        super(GlobalizationExtentions.localication(message));
    }

    public ValidatorException() {
    }

    @JsonIgnore
    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    @JsonIgnore
    @Override
    public synchronized Throwable getCause() {
        return null;
    }

    @JsonIgnore
    @Override
    public StackTraceElement[] getStackTrace() {
        return null;
    }
}