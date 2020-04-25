package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.jtownson.odysseyj.VC.VCBuilder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.jtownson.odysseyj.URICreator.isAbsoluteUri;
import static net.jtownson.odysseyj.URICreator.uri;

public class JsonCodec {

    public static JsonNode encode(VC vc) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode json = objectMapper.createObjectNode();
        putContext(vc, objectMapper, json);
        putId(vc, json);
        putType(vc, objectMapper, json);
        json.put("issuer", vc.getIssuer().toString());
        json.put("issuanceDate", dfRfc3339.format(vc.getIssuanceDate()));
        if (vc.getExpirationDate().isPresent()) {
            json.put("expirationDate", dfRfc3339.format(vc.getExpirationDate().get()));
        }

        if (vc.getCredentialSubjects().size() == 1) {
            json.set("credentialSubject", vc.getCredentialSubjects().get(0));
        } else if (vc.getCredentialSubjects().size() > 1) {
            ArrayNode credentialSubjects = objectMapper.createArrayNode();
            vc.getCredentialSubjects().forEach(credentialSubjects::add);
            json.set("credentialSubject", credentialSubjects);
        }

        return json;
    }

    public static VC decode(File json) throws ParseError {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return decode(objectMapper.readTree(json));
        } catch (IOException e) {
            throw new ParseError("Unable to parse json. Caught an IOException: " + e.getMessage());
        }
    }

    public static VC decode(JsonNode json) throws ParseError {
        VCBuilder builder = VC.builder();
        if (! json.getNodeType().equals(JsonNodeType.OBJECT)) {
            throw new ParseError("vc must be a JSON object.");
        }

        decodeId(json, builder);
        decodeType(json, builder);
        decodeContext(json, builder);
        decodeIssuer(json, builder);
        decodeIssuanceDate(json, builder);
        decodeExpirationDate(json, builder);
        decodeCredentialSubject(json, builder);
        return builder.build();
    }

    private static final DateTimeFormatter dfRfc3339 = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("UTC"));

    private static void decodeCredentialSubject(JsonNode json, VCBuilder builder) throws ParseError {
        JsonNode subjectNode = json.get("credentialSubject");
        if (subjectNode.isObject()) {
            builder.credentialSubject((ObjectNode)subjectNode);
        } else if (subjectNode.isArray()) {
            List<JsonNode> subjectNodes = toList(subjectNode.elements());
            for (JsonNode node : subjectNodes) {
                if (node.isObject()) {
                    builder.credentialSubject((ObjectNode) node);
                } else {
                    throw new ParseError("Invalid object type in credentialSubject: " + node.getNodeType());
                }
            }
        } else {
            throw new ParseError("credentialSubject must be either an object or array of objects.");
        }
    }

    private static void decodeType(JsonNode json, VCBuilder builder) throws ParseError {
        JsonNode typeNode = json.get("type");
        if (typeNode == null || ! typeNode.isArray()) {
            throw new ParseError("type field must be an array of the form [VerifiableCredential, <string values...>].");
        }

        List<JsonNode> types = toList(typeNode.elements());
        if (types.size() == 0) {
            throw new ParseError("type field must be an array of the form [VerifiableCredential, <string values...>].");
        } else {
            JsonNode t0 = types.get(0);
            if (t0.getNodeType() != JsonNodeType.STRING || !t0.asText().equals("VerifiableCredential")) {
                throw new ParseError("type field must be an array of the form [VerifiableCredential, <string values...>].");
            }
            builder.type("VerifiableCredential");

            for (int i = 1; i < types.size(); i++) {
                JsonNode tn = types.get(i);
                if (tn.getNodeType() != JsonNodeType.STRING) {
                    throw new ParseError("type field must be an array of the form [VerifiableCredential, <string values...>].");
                }
                builder.type(tn.asText());
            }
        }
    }

    private static void decodeContext(JsonNode json, VCBuilder builder) throws ParseError {
        String v1 = "https://www.w3.org/2018/credentials/v1";
        JsonNode contextNode = json.get("@context");
        if (contextNode.getNodeType() == JsonNodeType.STRING) {
            if (contextNode.asText().equals(v1)) {
                builder.context(uri(v1));
            } else {
                throw new ParseError("Context string must be " + v1);
            }
        }
        else if (contextNode.isArray()) {
            List<JsonNode> ctxs = toList(contextNode.elements());
            if (ctxs.size() == 0) {
                throw new ParseError(
                        "context field must be an array of the form ["+v1+", <uris...>].");
            } else if (ctxs.size() == 1) {
                throw new ParseError(
                        "context field must be an array of the form ["+v1+", <uris...>].");
            }
            else {
                JsonNode c0 = ctxs.get(0);
                if (c0.getNodeType() != JsonNodeType.STRING || !c0.asText().equals(v1)) {
                    throw new ParseError(
                            "context field must be an array of the form ["+v1+", <uris...>].");
                } else {
                    builder.context(uri(c0.asText()));
                }
                for (int i = 1; i < ctxs.size(); i++) {
                    JsonNode c = ctxs.get(i);
                    if (c.getNodeType() == JsonNodeType.STRING) {
                        String v = c.asText();
                        if (URICreator.isAbsoluteUri(v)) {
                            builder.context(uri(c.asText()));
                        } else {
                            throw new ParseError(v + " is not a valid URI");
                        }
                    }
                }
            }
        } else {
            throw new ParseError(
                    "context field must be an array of the form ["+v1+", <uris...>].");
        }
    }

    private static <T> List<T> toList(Iterator<T> i) {
        return toList(() -> i);
    }

    private static <T> List<T> toList(Iterable<T> i) {
        return StreamSupport
                .stream(i.spliterator(), false)
                .collect(Collectors.toList());
    }

    private static void decodeId(JsonNode json, VCBuilder builder) throws ParseError {
        JsonNode idNode = json.get("id");
        if (idNode != null) {
            if (idNode.getNodeType() != JsonNodeType.STRING) {
                throw new ParseError("value of id field must be a string.");
            }
            builder.id(idNode.asText());
        }
    }

    private static void decodeIssuer(JsonNode json, VCBuilder builder) throws ParseError {
        JsonNode issuer = json.get("issuer");
        if (issuer != null) {
            if (issuer.getNodeType() != JsonNodeType.STRING) {
                throw new ParseError("issuer must be a string.");
            } else {
                String v = issuer.asText();
                if (isAbsoluteUri(v)) {
                    builder.issuer(uri(issuer.asText()));
                } else {
                    throw new ParseError("Issuer must a valid URI. Got " + v);
                }
            }
        } else {
            throw new ParseError("issuer cannot be null");
        }
    }

    private static void decodeExpirationDate(JsonNode json, VCBuilder builder) throws ParseError {
        JsonNode expirationDate = json.get("expirationDate");
        if (expirationDate != null) {
            if (expirationDate.getNodeType() != JsonNodeType.STRING) {
                throw new ParseError("expirationDate must be a string.");
            } else {
                LocalDateTime exp = LocalDateTime.from(dfRfc3339.parse(expirationDate.asText()));
                builder.expirationDate(exp);
            }
        }
    }

    private static void decodeIssuanceDate(JsonNode json, VCBuilder builder) throws ParseError {
        JsonNode issuanceDate = json.get("issuanceDate");
        if (issuanceDate != null) {
            if (issuanceDate.getNodeType() != JsonNodeType.STRING) {
                throw new ParseError("issuanceDate must be a string.");
            } else {
                LocalDateTime iss = LocalDateTime.from(dfRfc3339.parse(issuanceDate.asText()));
                builder.issuanceDate(iss);
            }
        } else {
            throw new ParseError("issuer cannot be null");
        }
    }

    private static void putType(VC vc, ObjectMapper objectMapper, ObjectNode json) {
        ArrayNode type = objectMapper.createArrayNode();
        vc.getTypes().forEach(type::add);
        json.set("type", type);
    }

    private static void putId(VC vc, ObjectNode json) {
        if (vc.getId().isPresent()) {
            json.put("id", vc.getId().get());
        }
    }

    private static void putContext(VC vc, ObjectMapper objectMapper, ObjectNode json) {
        if (vc.getContexts().size() == 1) {
            json.put("@context", vc.getContexts().get(0).toString());
        } else if (vc.getContexts().size() > 1) {
            ArrayNode ctx = objectMapper.createArrayNode();
            vc.getContexts().forEach(uri -> ctx.add(uri.toString()));
            json.set("@context", ctx);
        }
    }
}
