package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.jtownson.odysseyj.VC.VCBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.jtownson.odysseyj.URICreator.isAbsoluteUri;
import static net.jtownson.odysseyj.URICreator.uri;

public class VCJsonCodec {

    public static JsonNode encode(VC vc) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode json = objectMapper.createObjectNode();
        putContext(vc.getContexts(), objectMapper, json);
        putType(vc.getTypes(), objectMapper, json);
        putId(vc.getId(), json);
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

        decodeId(json, builder::id);
        decodeType(json, builder::type, "VerifiableCredential");
        decodeContext(json, builder::context);
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

    static void decodeType(JsonNode json, Consumer<String> builder, String initialType) throws ParseError {
        JsonNode typeNode = json.get("type");
        if (typeNode == null || ! typeNode.isArray()) {
            throw new ParseError("type field must be an array of the form ["+initialType+", <string values...>].");
        }

        List<JsonNode> types = toList(typeNode.elements());
        if (types.size() == 0) {
            throw new ParseError("type field must be an array of the form ["+initialType+", <string values...>].");
        } else {
            JsonNode t0 = types.get(0);
            if (t0.getNodeType() != JsonNodeType.STRING || !t0.asText().equals(initialType)) {
                throw new ParseError("type field must be an array of the form ["+initialType+", <string values...>].");
            }
            builder.accept(initialType);

            for (int i = 1; i < types.size(); i++) {
                JsonNode tn = types.get(i);
                if (tn.getNodeType() != JsonNodeType.STRING) {
                    throw new ParseError("type field must be an array of the form ["+initialType+", <string values...>].");
                }
                builder.accept(tn.asText());
            }
        }
    }

    static void decodeContext(JsonNode json, Consumer<URI> builder) throws ParseError {
        String v1 = "https://www.w3.org/2018/credentials/v1";
        JsonNode contextNode = json.get("@context");
        if (contextNode.getNodeType() == JsonNodeType.STRING) {
            if (contextNode.asText().equals(v1)) {
                builder.accept(uri(v1));
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
                    builder.accept(uri(c0.asText()));
                }
                for (int i = 1; i < ctxs.size(); i++) {
                    JsonNode c = ctxs.get(i);
                    if (c.getNodeType() == JsonNodeType.STRING) {
                        String v = c.asText();
                        if (URICreator.isAbsoluteUri(v)) {
                            builder.accept(uri(c.asText()));
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

    static <T> List<T> toList(Iterator<T> i) {
        return toList(() -> i);
    }

    private static <T> List<T> toList(Iterable<T> i) {
        return StreamSupport
                .stream(i.spliterator(), false)
                .collect(Collectors.toList());
    }

    static void decodeId(JsonNode json, Consumer<String> builder) throws ParseError {
        JsonNode idNode = json.get("id");
        if (idNode != null) {
            if (idNode.getNodeType() != JsonNodeType.STRING) {
                throw new ParseError("value of id field must be a string.");
            }
            builder.accept(idNode.asText());
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

    static void putId(Optional<String> id, ObjectNode json) {
        id.ifPresent(s -> json.put("id", s));
    }

    static void putType(List<String> types, ObjectMapper objectMapper, ObjectNode json) {
        ArrayNode type = objectMapper.createArrayNode();
        types.forEach(type::add);
        json.set("type", type);
    }

    static void putContext(List<URI> contexts, ObjectMapper objectMapper, ObjectNode json) {
        if (contexts.size() == 1) {
            json.put("@context", contexts.get(0).toString());
        } else if (contexts.size() > 1) {
            ArrayNode ctx = objectMapper.createArrayNode();
            contexts.forEach(uri -> ctx.add(uri.toString()));
            json.set("@context", ctx);
        }
    }
}
