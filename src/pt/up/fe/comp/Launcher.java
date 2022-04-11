package pt.up.fe.comp;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Objects;

import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        // comp [-r=<num>] [-o] [-d] -i=<file.jmm>
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));
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

        System.out.println("input file     : " + inputFileStr);
        System.out.println("optimize flag  : " + optimize);
        System.out.println("register value : " + registerAllocation);
        System.out.println("debug flag     : " + debug);

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
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", inputFileStr);
        config.put("optimize", optimize);
        config.put("registerAllocation", registerAllocation);
        config.put("debug", debug);

        // Parse stage
        SimpleParser parser = new SimpleParser();
        JmmParserResult parserResult = parser.parse(input, config);

        for (Report report : parserResult.getReports()) {
            System.out.println(report);
        }
        
    }

}
