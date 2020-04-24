package net.jtownson.odysseyj;

public class InvalidSignature extends VerificationError {
    public InvalidSignature() {
        super("Invalid signature detected");
    }
}
