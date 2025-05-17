
package edu.kit.kastel.vads.compiler.backend.asm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;


public class CommandLineRunner {

    public CommandLineRunner() {
        rt = Runtime.getRuntime();
    }

    public void invokeGcc(Path input, Path output) throws IOException {
        String command = "gcc " + input.toString() + " -o " + output.toString();

        Process proc = rt.exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    
        String s;
        System.out.println("Output:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // Read any errors from the attempted command
        System.out.println("Errors:\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

    private final Runtime rt;
}
