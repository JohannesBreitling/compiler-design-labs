package edu.kit.kastel.vads.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.vads.compiler.backend.asm.CommandLineRunner;
import edu.kit.kastel.vads.compiler.backend.asm.L1AssemblerGenerator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.SsaTranslation;
import edu.kit.kastel.vads.compiler.ir.optimize.LocalValueNumbering;
import edu.kit.kastel.vads.compiler.ir.util.YCompPrinter;
import edu.kit.kastel.vads.compiler.lexer.Lexer;
import edu.kit.kastel.vads.compiler.parser.ParseException;
import edu.kit.kastel.vads.compiler.parser.Parser;
import edu.kit.kastel.vads.compiler.parser.TokenSource;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.semantic.SemanticAnalysis;
import edu.kit.kastel.vads.compiler.semantic.SemanticException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Invalid arguments: Expected one input file and one output file");
            System.exit(3);
        }

        Path inputPath = Path.of(args[0]);
        String output = args[1];
        Path outputAsmPath = Path.of(output + ".s");
        Path outputPath = Path.of(output);

        System.out.println("Input Path : " + inputPath.toString());
        System.out.println("Assembler Path : " + outputAsmPath.toString());
        System.out.println("Output Path : " + outputPath.toString());


        // Path input = Path.of(args[1]);
        // String filename = args[0];
        // Path output = Path.of(filename);

        // System.out.println("Input: " + args[0]);
        // System.out.println("Output: " + args[1]);

        ProgramTree program = lexAndParse(inputPath);
        
        try {
            new SemanticAnalysis(program).analyze();
        } catch (SemanticException e) {
            e.printStackTrace();
            System.exit(7);
            return;
        }
        List<IrGraph> graphs = new ArrayList<>();
        for (FunctionTree function : program.topLevelTrees()) {
            SsaTranslation translation = new SsaTranslation(function, new LocalValueNumbering());
            graphs.add(translation.translate());
        }

        if ("vcg".equals(System.getenv("DUMP_GRAPHS")) || "vcg".equals(System.getProperty("dumpGraphs"))) {
            Path tmp = outputPath.toAbsolutePath().resolveSibling("graphs");
            Files.createDirectory(tmp);
            for (IrGraph graph : graphs) {
                dumpGraph(graph, tmp, "before-codegen");
            }
        }
        
        // String s = new AbstractAssemblerGenerator().generateCode(graphs);
        // Files.writeString(output, s);
        
        
        String asm = new L1AssemblerGenerator().generateCode(graphs); 
        Files.writeString(outputAsmPath, asm);
        
        // Directly invoke gcc to link the assembly
        CommandLineRunner cmd = new CommandLineRunner();
        cmd.invokeGcc(outputAsmPath, outputPath);
    }

    private static ProgramTree lexAndParse(Path input) throws IOException {
        try {
            Lexer lexer = Lexer.forString(Files.readString(input));
            TokenSource tokenSource = new TokenSource(lexer);
            Parser parser = new Parser(tokenSource);
            return parser.parseProgram();
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(42);
            throw new AssertionError("unreachable");
        }
    }

    private static void dumpGraph(IrGraph graph, Path path, String key) throws IOException {
        Files.writeString(
            path.resolve(graph.name() + "-" + key + ".vcg"),
            YCompPrinter.print(graph)
        );
    }
}
