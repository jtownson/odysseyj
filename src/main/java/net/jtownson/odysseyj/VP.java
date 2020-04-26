package net.jtownson.odysseyj;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class VP {
    @Singular
    private final List<URI> contexts;
    private final String id;
    @Singular
    private final List<String> types;
    @Singular
    private final List<VC> verifiableCredentials;
    private final URI holder;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<URI> getHolder() {
        return Optional.ofNullable(holder);
    }

    public static VP fromJsonLd(File jsonLdFile) throws ParseError {
        return VPJsonCodec.decode(jsonLdFile);
    }
}
