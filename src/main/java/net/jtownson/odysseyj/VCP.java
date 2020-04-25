package net.jtownson.odysseyj;

import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;

public class VCP {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: VCP <file>");
            System.exit(1);
        }
        String fileName = args[0];
        File f = new File(fileName);
        try {
            VC.fromJsonLd(f);
            cat(f);
        } catch(ParseError e) {
            System.err.println("Error reading credential: " + e.getMessage());
        }
    }

    @SneakyThrows
    private static void cat(File f) {
        System.out.print(new String(Files.readAllBytes(f.toPath()), UTF_8));
    }
}
