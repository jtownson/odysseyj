package net.jtownson.odysseyj;

import lombok.Builder;
import lombok.Data;

import java.net.URL;
import java.security.PrivateKey;

@Data
@Builder
public class SignatureDefinition {
    private final String alg;
    private final URL publicKeyRef;
    private final PrivateKey privateKey;
}
