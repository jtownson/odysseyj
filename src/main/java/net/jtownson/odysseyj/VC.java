package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@ToString
public class VC {

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

        return "";
    }

//    public List<String> types() {
//        List<String> types = new ArrayList();
//        types.add("VerifiableCredential");
//        types.addAll(additionalTypes);
//        return unmodifiableList(types);
//    }
//
//    public List<URI> contexts() {
//        List<URI> contexts = new ArrayList();
//        contexts.add(uri("https://www.w3.org/2018/credentials/v1"));
//        contexts.addAll(additionalContexts);
//        return unmodifiableList(contexts);
//    }
}
