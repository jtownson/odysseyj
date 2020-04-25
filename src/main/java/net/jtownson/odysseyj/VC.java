package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.SneakyThrows;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import static java.util.Collections.unmodifiableList;
import static net.jtownson.odysseyj.URICreator.uri;

@Data
@Builder
public class VC {
    private final String id;
    private final URI issuer;
    private final LocalDateTime issuanceDate;
    private final LocalDateTime expirationDate;
    @Singular private final List<String> types;
    @Singular private final List<URI> contexts;
    @Singular private final List<ObjectNode> credentialSubjects;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<LocalDateTime> getExpirationDate() {
        return Optional.ofNullable(expirationDate);
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
        return JwsCodec.encodeJws(
                signatureDefinition.getPrivateKey(),
                signatureDefinition.getPublicKeyRef(),
                signatureDefinition.getAlg(),
                vc);
    }

    public static Future<VC> fromJws(List<String> algWhitelist, PublicKeyResolver publicKeyResolver, String jwsSer) {
        return JwsCodec.decodeJws(algWhitelist, publicKeyResolver, jwsSer);
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
