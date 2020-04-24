package net.jtownson.odysseyj;

import java.net.URL;
import java.security.PublicKey;
import java.util.concurrent.CompletableFuture;

public interface PublicKeyResolver {
    CompletableFuture<PublicKey> resolvePublicKey(URL publicKeyRef);
}
