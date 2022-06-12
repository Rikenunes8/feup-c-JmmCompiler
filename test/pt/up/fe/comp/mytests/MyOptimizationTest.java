package pt.up.fe.comp.mytests;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MyOptimizationTest {
    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("fixtures/public/optimizations/" + filename));
    }
    static OllirResult getOllirResultOpt(String filename) {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        return TestUtils.optimize(SpecsIo.getResource("fixtures/public/optimizations/" + filename), config);
    }

    static OllirResult getSetup(String filename) {
        var ollirResult = getOllirResult(filename);
        var ollirResultOpt = getOllirResultOpt(filename);
        TestUtils.noErrors(ollirResult.getReports());
        TestUtils.noErrors(ollirResultOpt.getReports());
        assertNotEquals(ollirResult.getOllirCode(), ollirResultOpt.getOllirCode());
        return ollirResultOpt;
    }

    @Test
    public void constant_propagation_while() {
        String ollirCode = getSetup("while.jmm").getOllirCode();

        String cond = ".*a\\.i32 <\\.bool 4\\.i32.*";
        String assignment = ".*c\\.i32 :=\\.i32 4\\.i32.*";
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(cond)));
        assertTrue(SpecsStrings.matches(ollirCode, Pattern.compile(assignment)));
    }

    @Test
    public void constant_propagation_while_nested() {
        String ollirCode = getSetup("while_nested.jmm").getOllirCode();

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
        String ollirCode = getSetup("if_true.jmm").getOllirCode();

        String assignment1 = "b.i32 :=.i32 1.i32";
        String assignment2 = "b.i32 :=.i32 2.i32";
        var gotoOccurOpt = countOccurences(ollirCode, "goto");

        assertEquals(0, gotoOccurOpt);
        assertTrue(ollirCode.contains(assignment1));
        assertFalse(ollirCode.contains(assignment2));
    }

    @Test
    public void dead_code_elimination_if_false() {
        String ollirCode = getSetup("if_false.jmm").getOllirCode();

        String assignment1 = "b.i32 :=.i32 1.i32";
        String assignment2 = "b.i32 :=.i32 2.i32";
        var gotoOccurOpt = countOccurences(ollirCode, "goto");

        assertEquals(0, gotoOccurOpt);
        assertFalse(ollirCode.contains(assignment1));
        assertTrue(ollirCode.contains(assignment2));

    }

    @Test
    public void dead_code_elimination_while() {
        String ollirCode = getSetup("while_false.jmm").getOllirCode();
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
        String ollirCode = getSetup("while_nested_false.jmm").getOllirCode();

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
}
