package net.jtownson.odysseyj;

public abstract class VerificationError extends Exception {
    public VerificationError(String message) {
        super(message);
    }
}
