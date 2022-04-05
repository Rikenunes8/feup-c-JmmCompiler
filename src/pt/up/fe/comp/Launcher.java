package pt.up.fe.comp;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        // comp [-r=<num>] [-o] [-d] -i=<file.jmm>
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));
        
        // Default Arguments Values 
        String registerAllocation = "-1"; // [-r] Type Integer
        

        // read the input code
        // at least the input file
        if (args.length < 1) {
            throw new RuntimeException("Expected at least a single argument, a path to an existing input file.");
        }
        // no more than the needed arguments
        if (args.length > 4) {
            throw new RuntimeException("Found too many arguments.");
        }

        // the last parameter is always the input file
        String inputFileStr = "";
        if (args[args.length-1].length() <= 3 || !args[args.length-1].substring(0, 3).equals("-i=") ) {
            throw new RuntimeException("The last argument must be a path to an existing input file: -i=<input_file.jmm>.");        
        } else {
            inputFileStr = args[args.length-1].substring(3);
        }

        // Repeted flags -o or -d not allowed
        if (Collections.frequency(Arrays.asList(args),"-o") > 1 || Collections.frequency(Arrays.asList(args),"-d") > 1) {
            throw new RuntimeException("The last argument must be a path to an existing input file: -i=<input_file.jmm>.");      
        }
        String optimize = String.valueOf(Arrays.asList(args).contains("-o"));
        System.out.println(optimize);
        String debug = String.valueOf(Arrays.asList(args).contains("-d"));
        // ArrayUtils.contains( fieldsToInclude, "id" )
        // order between optional arguments do not matter


        System.out.println("input file:" + inputFileStr);
        System.out.println("optimize flag:" + optimize);
        System.out.println("register value:" + registerAllocation);
        System.out.println("debug flag:" + debug);
        
        File inputFile = new File(inputFileStr);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFileStr + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", inputFileStr);
        config.put("optimize", optimize);
        config.put("registerAllocation", registerAllocation);
        config.put("debug", debug);

        // Instantiate JmmParser
        // SimpleParser parser = new SimpleParser();

        // Parse stage
        // JmmParserResult parserResult = parser.parse(input, config);
        // System.out.println(parserResult.getRootNode().toTree());
        // Check if there are parsing errors
        // TestUtils.noErrors(parserResult.getReports());

        // ... add remaining stages
    }

}
