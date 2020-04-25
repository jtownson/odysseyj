package net.jtownson.odysseyj;

import lombok.SneakyThrows;

import java.net.URI;
import java.net.URL;

public class URICreator {
    public static boolean isAbsoluteUri(String maybeUri) {
        try {
            return URI.create(maybeUri).isAbsolute();
        } catch(Exception e) {
            return false;
        }
    }

    public static URI uri(String uri) {
        return URI.create(uri);
    }

    @SneakyThrows
    public static URL url(String url) {
        return new URL(url);
    }
}
