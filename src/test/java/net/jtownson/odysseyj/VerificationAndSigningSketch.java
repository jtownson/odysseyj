package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.singletonList;
import static net.jtownson.odysseyj.URICreator.uri;
import static org.jose4j.jws.AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256;

public class VerificationAndSigningSketch {
    @Test
    public void generateAndParseVerifiableCredentials() throws Exception {
        SignatureDefinition signatureDefinition = KeyFoo.getKeyPair();

        String jws = VC.jwsBuilder()
                .additionalType("AddressCredential")
                .additionalContext(uri("https://www.w3.org/2018/credentials/examples/v1"))
                .id("https://www.postoffice.co.uk/addresses/1234")
                .issuer(uri("https://www.postoffice.co.uk"))
                .issuanceDate(LocalDate.of(2020, 1, 1).atStartOfDay())
                .expirationDate(LocalDate.of(2021, 1, 1).atStartOfDay())
                .credentialSubject(createSubject())
                .signatureDefinition(signatureDefinition)
                .build();

        System.out.println("Generated JWS for wire transfer: ");
        System.out.println(jws);

        VC vc = VC.fromJws(singletonList(ECDSA_USING_P256_CURVE_AND_SHA256), dummyKeyResolver(), jws).get();

        System.out.println("Received dataset has a valid signature and decodes to the following dataset: " + vc);
//        System.out.println(println(VCJsonCodec.vcJsonEncoder(parseResult).printWith(Printer.spaces2))
    }

    private static PublicKeyResolver dummyKeyResolver() {
        return publicKeyRef -> CompletableFuture.completedFuture(KeyFoo.getPublicKeyFromRef(publicKeyRef));
    }

    private ObjectNode createSubject() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode subject = objectMapper.createObjectNode();
        subject.put("id", "did:ata:abc123");
        subject.put("name", "Her Majesty The Queen");
        subject.put("address", "Buckingham Palace, SW1A 1AA");
        return subject;
    }
}
