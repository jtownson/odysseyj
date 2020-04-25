package net.jtownson.odysseyj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import static java.util.Collections.emptyList;

public class JsonCodec {

    private static DateTimeFormatter dfRfc3339 = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("UTC"));

    public static VC decode(JsonNode jsonNode) {
        return new VC("", URICreator.uri("test:uri"), LocalDateTime.now(), LocalDateTime.now(), emptyList(), emptyList(), emptyList());
    }

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
//        Encoder.instance { vc: VC =>
//            obj(
//                    "@context" -> strOrArr(vc.contexts),
//                    "id" -> vc.id.map(_.asJson).getOrElse(Json.Null),
//                    "type" -> strOrArr(vc.types),
//                    "issuer" -> vc.issuer.asJson,
//                    "issuanceDate" -> vc.issuanceDate.asJson,
//                    "expirationDate" -> vc.expirationDate.map(ldt => ldt.asJson).getOrElse(Json.Null),
//                    "credentialSubject" -> strOrArr(vc.subjects)
//      ).dropNullValues
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

    public static void traverse(JsonNode root){

        if(root.isObject()){
            Iterator<String> fieldNames = root.fieldNames();

            while(fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                traverse(fieldValue);
            }
        } else if(root.isArray()){
            ArrayNode arrayNode = (ArrayNode) root;
            for(int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                traverse(arrayElement);
            }
        } else {
            // JsonNode root represents a single value field - do something with it.

        }
    }
}
