package pt.up.fe.comp.mytests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.JmmOptimizer;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
    public void register_allocation() {
        String ollirCode = SpecsIo.getResource("fixtures/public/temp.ollir");
        JmmOptimizer optimizer = new JmmOptimizer();
        OllirResult ollirResult = new OllirResult(ollirCode, Collections.emptyMap());
        optimizer.optimize(ollirResult);
    }

}
