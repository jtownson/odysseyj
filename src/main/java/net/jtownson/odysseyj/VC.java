package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.SneakyThrows;
import org.jose4j.jws.JsonWebSignature;

import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static net.jtownson.odysseyj.URICreator.uri;

@Data
public class VC {
    private final String id;
    private final URI issuer;
    private final LocalDateTime issuanceDate;
    private final LocalDateTime expirationDate;
    private final List<String> types;
    private final List<URI> contexts;
    private final List<ObjectNode> credentialSubjects;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<LocalDateTime> getExpirationDate() {
        return Optional.ofNullable(expirationDate);
    }

    public List<String> getTypes() {
        return types(emptyList());
    }

    public List<URI> getContexts() {
        return contexts(emptyList());
    }

    @SneakyThrows
    @Builder(builderMethodName = "jwsBuilder")
    public static String toJws(
            @Singular List<String> additionalTypes,
            @Singular List<URI> additionalContexts,
            @Singular List<ObjectNode> credentialSubjects,
            URI issuer,
            SignatureDefinition signatureDefinition,
            String id,
            LocalDateTime issuanceDate,
            LocalDateTime expirationDate) {

        VC vc = new VC(id, issuer, issuanceDate, expirationDate, types(additionalTypes), contexts(additionalContexts), credentialSubjects);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setKey(signatureDefinition.getPrivateKey());
        jws.getHeaders().setFullHeaderAsJsonString(headers(signatureDefinition.getPublicKeyRef(), signatureDefinition.getAlg(), vc));
        return jws.getCompactSerialization();
    }

    private static String headers(URL publicKeyRef, String alg, VC vc) {
        ObjectMapper om = new ObjectMapper();
        ObjectNode n = om.createObjectNode();
        n.put("cty", "application/vc+json");
        n.put("kid", publicKeyRef.toString());
        n.put("alg", alg);
        n.put("iss", vc.issuer.toString());
        n.put("nbf", vc.issuanceDate.toEpochSecond(ZoneOffset.UTC));
        if (vc.expirationDate != null) {
            n.put("exp", vc.expirationDate.toEpochSecond(ZoneOffset.UTC));
        }
        n.put("vc", "<encoded vc>");
        return n.toString();
    }

    private static List<String> types(List<String> additionalTypes) {
        List<String> types = new ArrayList();
        types.add("VerifiableCredential");
        types.addAll(additionalTypes);
        return unmodifiableList(types);
    }

    private static List<URI> contexts(List<URI> additionalContexts) {
        List<URI> contexts = new ArrayList();
        contexts.add(uri("https://www.w3.org/2018/credentials/v1"));
        contexts.addAll(additionalContexts);
        return unmodifiableList(contexts);
    }
}
