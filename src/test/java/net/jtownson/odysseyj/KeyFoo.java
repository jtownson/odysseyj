package net.jtownson.odysseyj;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.jose4j.jws.AlgorithmIdentifiers;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PublicKey;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyFoo {

    private static String getKey(Path path) throws IOException {
        return new String(Files.readAllBytes(path), UTF_8);
    }

    public static SignatureDefinition getKeyPair() throws IOException {
        Path keyFile = Paths.get("id_ecdsa.pem");
        URL publicKeyURL = keyFile.toFile().toURI().toURL();
        return new SignatureDefinition(
                AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256,
                publicKeyURL,
                getKeyPair(keyFile).getPrivate());
    }

    public static PublicKey getPublicKeyFromRef(URL publicKeyRef) throws URISyntaxException, IOException {
        return getKeyPair(Paths.get(publicKeyRef.toURI())).getPublic();
    }

    private static KeyPair getKeyPair(Path path) throws IOException {
        return getKeyPair(getKey(path));
    }

    private static KeyPair getKeyPair(String key) throws IOException {
        StringReader keyReader = new StringReader(key);
        PEMParser parser = new PEMParser(keyReader);
        PEMKeyPair pemPair = (PEMKeyPair) (parser.readObject());
        return new JcaPEMKeyConverter().getKeyPair(pemPair);
    }
}
