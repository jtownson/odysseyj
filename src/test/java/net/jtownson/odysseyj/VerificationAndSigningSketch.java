package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Optional;

import static net.jtownson.odysseyj.URICreator.uri;

public class VerificationAndSigningSketch {
    @Test
    public void generateAndParseVerifiableCredentials() throws IOException, URISyntaxException {
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

        System.out.println(jws);
    }

    private ObjectNode createSubject() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode subject = objectMapper.createObjectNode();
        subject.put("id", "did:ata:abc123");
        subject.put("name", "Her Majesty The Queen");
        subject.put("address", "Buckingham Palance, SW1A 1AA");
        return subject;
    }
}
