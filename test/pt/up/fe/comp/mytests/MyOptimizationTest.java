package pt.up.fe.comp.mytests;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.JmmOptimizer;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static pt.up.fe.comp.TestUtils.analyse;

public class MyOptimizationTest {
    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("fixtures/public/optimizations/" + filename));
    }
    static OllirResult getOllirResultOpt(String filename, int type) { // 0 - all; 1 - ast; 2 - ollir
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");

        String jmmCode = SpecsIo.getResource("fixtures/public/optimizations/" + filename);
        var semanticsResult = TestUtils.analyse(jmmCode, config);
        TestUtils.noErrors(semanticsResult.getReports());

        JmmOptimization optimization = TestUtils.getJmmOptimization();
        if (type != 2) semanticsResult = optimization.optimize(semanticsResult);
        var ollirResult = optimization.toOllir(semanticsResult);
        if (type != 1) ollirResult = optimization.optimize(ollirResult);

        return ollirResult;
    }

    static OllirResult getSetup(String filename, int type) { // type: 0 - all; 1 - ast; 2 - ollir
        var ollirResult = getOllirResult(filename);
        var ollirResultOpt = getOllirResultOpt(filename, type);
        TestUtils.noErrors(ollirResult.getReports());
        TestUtils.noErrors(ollirResultOpt.getReports());
        assertNotEquals(ollirResult.getOllirCode(), ollirResultOpt.getOllirCode());
        return ollirResultOpt;
    }

    @Test
    public void constant_propagation_while() {
        String ollirCode = getSetup("while.jmm", 1).getOllirCode();

        String cond = ".*a\\.i32 <\\.bool 4\\.i32.*";
        String assignment = ".*c\\.i32 :=\\.i32 4\\.i32.*";
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(cond)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(assignment)));
    }

    @Test
    public void constant_propagation_while_nested() {
        String ollirCode = getSetup("while_nested.jmm", 1).getOllirCode();

        String cond1 = ".*a\\.i32 <\\.bool 4\\.i32.*";
        String cond2 = ".*c\\.i32 <\\.bool 4\\.i32.*";
        String assignment1 = ".*a\\.i32 :=\\.i32 a\\.i32 \\+.i32 1.i32.*";
        String assignment2 = ".*c\\.i32 :=\\.i32 5\\.i32.*";
        String assignment3 = ".*d\\.i32 :=\\.i32 4\\.i32.*";
        String assignment4 = ".*a\\.i32 :=\\.i32 a\\.i32 \\+.i32 4.i32.*";
        String assignment5 = ".*d\\.i32 :=\\.i32 5\\.i32.*";
        String assignment6 = ".*a\\.i32 :=\\.i32 a\\.i32 \\+.i32 5.i32.*";
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(cond1)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(cond2)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(assignment1)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(assignment2)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(assignment3)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(assignment4)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(assignment5)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(assignment6)));
    }

    @Test
    public void dead_code_elimination_if_true() {
        String ollirCode = getSetup("if_true.jmm", 1).getOllirCode();

        String assignment1 = "b.i32 :=.i32 1.i32";
        String assignment2 = "b.i32 :=.i32 2.i32";
        var gotoOccurOpt = countOccurences(ollirCode, "goto");

        assertEquals(0, gotoOccurOpt);
        assertTrue(ollirCode.contains(assignment1));
        assertFalse(ollirCode.contains(assignment2));
    }

    @Test
    public void dead_code_elimination_if_false() {
        String ollirCode = getSetup("if_false.jmm", 1).getOllirCode();

        String assignment1 = "b.i32 :=.i32 1.i32";
        String assignment2 = "b.i32 :=.i32 2.i32";
        var gotoOccurOpt = countOccurences(ollirCode, "goto");

        assertEquals(0, gotoOccurOpt);
        assertFalse(ollirCode.contains(assignment1));
        assertTrue(ollirCode.contains(assignment2));

    }

    @Test
    public void dead_code_elimination_while() {
        String ollirCode = getSetup("while_false.jmm", 1).getOllirCode();
        String assignment1 = "a.i32 :=.i32 0.i32";
        String assignment2 = "a.i32 :=.i32 a.i32 +.i32 1.i32";
        String returnV = "ret.V";
        var gotoOccurOpt = countOccurences(ollirCode, "goto");

        assertEquals(0, gotoOccurOpt);
        assertTrue(ollirCode.contains(assignment1));
        assertFalse(ollirCode.contains(assignment2));
        assertTrue(ollirCode.contains(returnV));
    }

    @Test
    public void dead_code_elimination_while_nested() {
        String ollirCode = getSetup("while_nested_false.jmm", 1).getOllirCode();

        String assignment1 = "a.i32 :=.i32 1.i32";
        String assignment2 = "b.i32 :=.i32 1.i32";
        var gotoOccurOpt = countOccurences(ollirCode, "goto");

        assertEquals(3, gotoOccurOpt);
        assertFalse(ollirCode.contains(assignment1));
        assertTrue(ollirCode.contains(assignment2));
    }

    public static int countOccurences(String code, String word) {
        return (code.length() - code.replace(word, "").length()) / word.length();
    }

    @Test // TODO do something to this test latter
    public void register_allocation() {
        String ollirCode = SpecsIo.getResource("fixtures/public/temp.ollir");
        JmmOptimizer optimizer = new JmmOptimizer();
        Map<String, String> config = new HashMap<>();
        config.put("registerAllocation", String.valueOf(5));
        OllirResult ollirResult = new OllirResult(ollirCode, config);
        optimizer.optimize(ollirResult);
    }
}
