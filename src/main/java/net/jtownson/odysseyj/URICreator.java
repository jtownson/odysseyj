package net.jtownson.odysseyj;

import lombok.SneakyThrows;

import java.net.URI;
import java.net.URL;

public class URICreator {
    @SneakyThrows
    public static URI uri(String uri) {
        return new URI(uri);
    }

    @SneakyThrows
    public static URL url(String url) {
        return new URL(url);
    }
}
