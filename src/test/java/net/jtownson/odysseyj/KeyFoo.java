package net.jtownson.odysseyj;

import lombok.SneakyThrows;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.jose4j.jws.AlgorithmIdentifiers;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PublicKey;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyFoo {

    public static SignatureDefinition getKeyPair() throws IOException {
        Path keyFile = Paths.get("id_ecdsa.pem");
        URL publicKeyURL = keyFile.toFile().toURI().toURL();
        return new SignatureDefinition(
                AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256,
                publicKeyURL,
                getKeyPair(keyFile).getPrivate());
    }

    @SneakyThrows
    public static PublicKey getPublicKeyFromRef(URL publicKeyRef) {
        return getKeyPair(Paths.get(publicKeyRef.toURI())).getPublic();
    }

    @SneakyThrows
    private static KeyPair getKeyPair(Path path) {
        return getKeyPair(getKey(path));
    }

    @SneakyThrows
    private static KeyPair getKeyPair(String key) {
        StringReader keyReader = new StringReader(key);
        PEMParser parser = new PEMParser(keyReader);
        PEMKeyPair pemPair = (PEMKeyPair) (parser.readObject());
        return new JcaPEMKeyConverter().getKeyPair(pemPair);
    }

    private static String getKey(Path path) throws IOException {
        return new String(Files.readAllBytes(path), UTF_8);
    }
}
