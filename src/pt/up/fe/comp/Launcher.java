package pt.up.fe.comp;

import java.io.File;
import java.io.IOException;
import java.util.*;

import pt.up.fe.comp.jasmin.JasminEmitter;
import pt.up.fe.comp.analysis.JmmAnalyser;
import pt.up.fe.comp.ast.SimpleParser;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.ollir.JmmOptimizer;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) throws IOException {
        SpecsSystem.programStandardInit();

        Map<String, String> config = new HashMap<>();
        String input = parseArgs(args, config);


        // Parse stage ----------------------------------
        System.out.println("Executing parsing stage ...");
        SimpleParser parser = new SimpleParser();
        JmmParserResult parserResult = parser.parse(input, config);
        if (verifyReports(parserResult.getReports())) return;
        printAST(parserResult.getRootNode(), false);


        // Analysis stage --------------------------------
        System.out.println("Executing analysis stage ...");
        JmmAnalyser analyser = new JmmAnalyser();
        JmmSemanticsResult semanticsResult = analyser.semanticAnalysis(parserResult);
        if (verifyReports(semanticsResult.getReports())) return;
        printSymbolTable(semanticsResult);


        // Optimization stage -----------------------------
        if (Utils.optimize) System.out.println("Executing AST optimization ...");
        JmmOptimizer optimizer = new JmmOptimizer();
        JmmSemanticsResult optSemanticsResult = optimizer.optimize(semanticsResult);
        printAST(optSemanticsResult.getRootNode(), true);

        System.out.println("Executing Ollir generation ...");
        OllirResult optimizationResult = optimizer.toOllir(optSemanticsResult);
        printOllir(optimizationResult, false);

        if (Utils.optimize) System.out.println("Executing Ollir optimization ...");
        OllirResult optOptimizationResult = optimizer.optimize(optimizationResult);
        if (verifyReports(optOptimizationResult.getReports())) return;
        printOllir(optOptimizationResult, true);


        // JasminBackend stage -------------------------------
        System.out.println("Executing Jasmin Backend stage ...");
        JasminEmitter jasminEmitter = new JasminEmitter();
        JasminResult jasminResult = jasminEmitter.toJasmin(optOptimizationResult);
        if (verifyReports(jasminResult.getReports())) return;
        printJasmin(jasminResult);


        // Saving generated file stage ------------------------
        System.out.println("Saving compilation in ./out/ ...");
        jasminResult.compile(new File("./out/"));

        System.out.println("\nCompilation successfully completed!");
    }

    private static String parseArgs(String[] args, Map<String, String> config) {
        // comp [-r=<num>] [-o] [-d] -i=<file.jmm>
        SpecsLogs.info("Executing with args: " + Arrays.toString(args) + "\n");
        String correctInput = "Correct input: .\\comp2022-1b [-r=<num>] [-o] [-d] -i=<file.jmm>";

        // At least the input file (mandatory argument)
        if (args.length < 1) {
            throw new RuntimeException("Expected at least a single argument, a path to an existing input file. " + correctInput);
        }
        // No more than the needed arguments
        if (args.length > 4) {
            throw new RuntimeException("Found too many arguments. " + correctInput);
        }
        // Found invalid arguments: only -r=.., -o, -d and -i=.. are allowed
        if (Arrays.stream(args).anyMatch(arg -> (!arg.startsWith("-r=") && !Objects.equals(arg, "-o") && !Objects.equals(arg, "-d") && !arg.startsWith("-i=")))) {
            throw new RuntimeException("Found invalid arguments: only -r=.., -o, -d and -i=.. are allowed. " + correctInput);
        }

        // Found repeated flags
        if ((Arrays.stream(args).filter(arg -> arg.startsWith("-r=")).count() > 1)
                || (Collections.frequency(Arrays.asList(args),"-o") > 1)
                || (Collections.frequency(Arrays.asList(args),"-d") > 1)) {
            throw new RuntimeException("Found repeated flags. " + correctInput);
        }
        // Not found the directive for the input file
        if (Arrays.stream(args).filter(arg -> arg.startsWith("-i=")).count() != 1) {
            throw new RuntimeException("A path to one existing input file is a mandatory argument. " + correctInput);
        }

        // Get Arguments Values [Order between arguments do not matter]
        String registerAllocation = (Arrays.stream(args).noneMatch(arg -> arg.startsWith("-r="))) ? "-1" : Arrays.stream(args).filter(arg -> arg.startsWith("-r=")).findFirst().get().substring(3);
        String optimize = String.valueOf(Arrays.asList(args).contains("-o"));
        String debug = String.valueOf(Arrays.asList(args).contains("-d"));
        String inputFileStr = Arrays.stream(args).filter(arg -> arg.startsWith("-i=")).findFirst().get().substring(3);

        Utils.debug = debug.equals("true");
        Utils.optimize = optimize.equals("true");

        if (Utils.debug) {
            System.out.println("input file     : " + inputFileStr);
            System.out.println("optimize flag  : " + optimize);
            System.out.println("register value : " + registerAllocation);
            System.out.println("debug flag     : " + debug + "\n");
        }

        // Check -r option : <num> is an integer between 0 and 255 [or -1 that is equals to not having]
        if (!registerAllocation.matches("\\b(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\b") && !Objects.equals(registerAllocation, "-1")) {
            throw new RuntimeException("Expected a number between 0 and 255, got -r='" + registerAllocation + "'.");
        }

        // Is a path to an existing input file
        File inputFile = new File(inputFileStr);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got -i='" + inputFileStr + "'.");
        }
        String input = SpecsIo.read(inputFile);


        // Create config
        config.put("inputFile", inputFileStr);
        config.put("optimize", optimize);
        config.put("registerAllocation", registerAllocation);
        config.put("debug", debug);

        return input;
    }

    private static boolean verifyReports(List<Report> reports) {
        if (!reports.isEmpty()) {
            for (Report report : reports) System.out.println(report);
            return true;
        }
        return false;
    }

    private static void printAST(JmmNode root, boolean optimized) {
        if (Utils.debug) {
            if (!optimized || (optimized && Utils.optimize)) {
                if (optimized) Utils.printHeader("AST OPTIMIZED");
                else Utils.printHeader("AST");
                System.out.println(root.sanitize().toTree());
                Utils.printFooter();
            }
        }
    }

    private static void printSymbolTable(JmmSemanticsResult semanticsResult) {
        // Print the SymbolTable
        if (Utils.debug) {
            Utils.printHeader("SYMBOL TABLE");
            System.out.println(semanticsResult.getSymbolTable().print());
            Utils.printFooter();
        }
    }

    private static void printOllir(OllirResult optimizationResult, boolean optimized) {
        if (Utils.debug) {
            if (!optimized || (optimized && Utils.optimize)) {
                if (optimized) Utils.printHeader("OLLIR OPTIMIZED");
                else Utils.printHeader("OLLIR");
                System.out.println(optimizationResult.getOllirCode());
                Utils.printFooter();
            }
        }
    }

    private static void printJasmin(JasminResult jasminResult) {
        // Print the Jasmin Code
        if (Utils.debug) {
            Utils.printHeader("JASMIN");
            System.out.println(jasminResult.getJasminCode());
            Utils.printFooter();
        }
    }

}
