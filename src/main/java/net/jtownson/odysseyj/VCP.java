package net.jtownson.odysseyj;

import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

@Command(description = "App to handle w3c test cases.",
        name = "vcp", mixinStandardHelpOptions = true)
public class VCP implements Callable<Integer> {

    @Parameters(index = "0", description = "The filename of the verfiable credential or presentation.")
    private File file;

    @Option(names = {"-t", "--type"}, description = "VerifiableCredential or VerifiablePresentation")
    private String type = "VerifiableCredential";

    public static void main(String[] args) {
        int ret = new CommandLine(new VCP()).execute(args);
        System.exit(ret);
    }

    @SneakyThrows
    private static void cat(File f) {
        System.out.print(new String(Files.readAllBytes(f.toPath()), UTF_8));
    }

    @Override
    public Integer call() {
        try {
            if ("VerifiableCredential".equals(type)) {
                VC.fromJsonLd(file);
            } else if ("VerifiablePresentation".equals(type)) {
                VP.fromJsonLd(file);
            } else {
                System.err.println("Illegal type argument: " + type +
                        ". Require one of VerifiableCredential or VerifiablePresentation");
                return 1;
            }
            cat(file);
            return 0;
        } catch(ParseError e) {
            System.err.println("Error processing file. Got an error: " + e.getMessage());
            return 1;
        }
    }
}
