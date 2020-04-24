package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.JsonWebSignature;

import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.Collections.emptyList;
import static net.jtownson.odysseyj.URICreator.url;

public class JwsCodec {

    @SneakyThrows
    public static String encodeJws(PrivateKey privateKey, URL publicKeyRef, String alg, VC vc) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setKey(privateKey);
        jws.getHeaders().setFullHeaderAsJsonString(headers(publicKeyRef, alg, vc));
        return jws.getCompactSerialization();
    }

    private static String headers(URL publicKeyRef, String alg, VC vc) {
        ObjectMapper om = new ObjectMapper();
        ObjectNode n = om.createObjectNode();
        n.put("cty", "application/vc+json");
        n.put("kid", publicKeyRef.toString());
        n.put("alg", alg);
        n.put("iss", vc.getIssuer().toString());
        n.put("nbf", vc.getIssuanceDate().toEpochSecond(ZoneOffset.UTC));
        vc.getExpirationDate().map(exp -> exp.toEpochSecond(ZoneOffset.UTC));
        n.put("vc", "<encoded vc>");
        return n.toString();
    }

    @SneakyThrows
    public static Future<VC> decodeJws(List<String> algWhitelist, PublicKeyResolver publicKeyResolver, String jwsSer) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(jwsSer);
        jws.setAlgorithmConstraints(
                new AlgorithmConstraints(
                        AlgorithmConstraints.ConstraintType.WHITELIST,
                        algWhitelist.toArray(new String[0])));
        URL publicKeyRef = url(jws.getHeader("kid"));

        CompletableFuture<PublicKey> publicKeyF = publicKeyResolver.resolvePublicKey(publicKeyRef);

        return publicKeyF
                .thenAccept(publicKey -> verifySignature(jws, publicKey))
                .thenApply(v -> parseVc(jws));
    }

    private static VC parseVc(JsonWebSignature jws) {
        String vc = jws.getHeader("vc");
        System.out.println(vc);
        return new VC("", URICreator.uri("test:uri"), LocalDateTime.now(), LocalDateTime.now(), emptyList(), emptyList(), emptyList());
    }

    @SneakyThrows
    private static void verifySignature(JsonWebSignature jws, PublicKey publicKey) {
        jws.setKey(publicKey);
        if ( ! jws.verifySignature()) {
            throw new InvalidSignature();
        }
    }
}
