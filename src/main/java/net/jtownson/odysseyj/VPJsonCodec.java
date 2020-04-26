package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static net.jtownson.odysseyj.URICreator.isAbsoluteUri;
import static net.jtownson.odysseyj.URICreator.uri;
import static net.jtownson.odysseyj.VCJsonCodec.*;

public class VPJsonCodec {

    public static JsonNode encode(VP vp) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode json = objectMapper.createObjectNode();
        putContext(vp.getContexts(), objectMapper, json);
        putType(vp.getTypes(), objectMapper, json);
        putId(vp.getId(), json);
        putHolder(vp, json);
        putProof(objectMapper, json);
        ArrayNode verifiableCredentialNode = objectMapper.createArrayNode();
        vp.getVerifiableCredentials().forEach(vc -> verifiableCredentialNode.add(VCJsonCodec.encode(vc)));
        json.set("verifiableCredential", verifiableCredentialNode);
        return json;
    }

    private static void putProof(ObjectMapper objectMapper, ObjectNode json) {
        json.set("proof", objectMapper.createArrayNode());
    }

    private static void putHolder(VP vp, ObjectNode json) {
        vp.getHolder().ifPresent(uri -> json.put("holder", uri.toString()));
    }

    public static VP decode(JsonNode json) throws ParseError {
        VP.VPBuilder builder = VP.builder();
        decodeId(json, builder::id);
        decodeType(json, builder::type, "VerifiablePresentation");
        decodeContext(json, builder::context);
        decodeHolder(json, builder);
        decodeVerifiableCredential(json, builder);
        decodeProof(json);
        return builder.build();
    }

    private static void decodeProof(JsonNode json) throws ParseError {
        JsonNode proofNode = json.get("proof");
        if (proofNode == null) {
            throw new ParseError("Proof node must be defined (questionable w3c testcase).");
        }
    }
    private static void decodeVerifiableCredential(JsonNode json, VP.VPBuilder builder) throws ParseError {
        JsonNode vcNode = json.get("verifiableCredential");
        if (vcNode.getNodeType() == JsonNodeType.OBJECT) {
            VC vc = VCJsonCodec.decode(vcNode);
            builder.verifiableCredential(vc);
        } else if (vcNode.getNodeType() == JsonNodeType.ARRAY) {
            List<JsonNode> vcNodes = toList(vcNode.elements());
            for (JsonNode vcn : vcNodes) {
                if (vcn.getNodeType() == JsonNodeType.OBJECT) {
                    builder.verifiableCredential(VCJsonCodec.decode(vcn));
                } else {
                    throw new ParseError("Invalid object type for verifiableCredential element: " + vcNode.getNodeType());
                }
            }
        }
    }

    private static void decodeHolder(JsonNode json, VP.VPBuilder builder) throws ParseError {
        JsonNode holderNode = json.get("holder");
        if (holderNode != null) {
            if (holderNode.getNodeType() != JsonNodeType.STRING || ! isAbsoluteUri(holderNode.asText())) {
                throw new ParseError("holder must be a valid URI");
            }
            builder.holder(uri(holderNode.asText()));
        }
    }

    public static VP decode(File jsonLdFile) throws ParseError {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return decode(objectMapper.readTree(jsonLdFile));
        } catch (IOException e) {
            throw new ParseError("Unable to parse json. Caught an IOException: " + e.getMessage());
        }
    }
}
