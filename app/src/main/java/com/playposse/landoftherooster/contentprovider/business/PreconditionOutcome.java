package com.playposse.landoftherooster.contentprovider.business;

/**
 * Represents the outcome of a precondition. The main purpose is to indicate if an action should
 * be carried out. The Precondition may attach additional information, that was processed during
 * evaluation and may be useful for later.
 */
public class PreconditionOutcome {

    private final Boolean isSuccess;

    public PreconditionOutcome(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Boolean getSuccess() {
        return isSuccess;
    }
}
