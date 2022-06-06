package pt.up.fe.comp.mytests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import static org.junit.Assert.assertEquals;

public class MyCompilerTest {

    private static void noErrors(String jmmCode, String expected) {
        var result = TestUtils.backend(jmmCode);
        System.out.println(result.getJasminCode());
        result.compile();
        var output = result.run();
        assertEquals(expected, output.trim());
    }

    @Test
    public void testIfStmtLogic1() {
        noErrors(SpecsIo.getResource("fixtures/public/run/IfStmtLogic1.jmm"), "3");
    }

    @Test
    public void testIfStmtLogic2() {
        noErrors(SpecsIo.getResource("fixtures/public/run/IfStmtLogic2.jmm"), "1");
    }

    @Test
    public void testIfStmtLogic3() {
        noErrors(SpecsIo.getResource("fixtures/public/run/IfStmtLogic3.jmm"), "3");
    }

    @Test
    public void testIfStmtLogic4() {
        noErrors(SpecsIo.getResource("fixtures/public/run/IfStmtLogic4.jmm"), "1");
    }

    @Test
    public void testIfStmtLogic5() {
        noErrors(SpecsIo.getResource("fixtures/public/run/IfStmtLogic5.jmm"), "1");
    }

    @Test
    public void testFunctionCall1() {
        noErrors(SpecsIo.getResource("fixtures/public/run/FunctionCall1.jmm"), "3");
    }

    @Test
    public void testFunctionCall2() {
        noErrors(SpecsIo.getResource("fixtures/public/run/FunctionCall2.jmm"), "3");
    }

    @Test
    public void testNestedFunctionCall() {
        noErrors(SpecsIo.getResource("fixtures/public/run/NestedFunctionCall.jmm"), "8");
    }

    @Test
    public void testArithmeticExpressionMul() {
        noErrors(SpecsIo.getResource("fixtures/public/run/ArithmeticExpressionMul.jmm"), "2");
    }
    @Test
    public void testArithmeticExpressionDiv() {
        noErrors(SpecsIo.getResource("fixtures/public/run/ArithmeticExpressionDiv.jmm"), "2");
    }

    @Test
    public void testArithmeticExpressionAll() {
        noErrors(SpecsIo.getResource("fixtures/public/run/ArithmeticExpressionAll.jmm"), "2");
    }

    @Test
    public void testArrays1() {
        noErrors(SpecsIo.getResource("fixtures/public/run/Arrays1.jmm"), "15");
    }

    @Test
    public void testArrays2() {
        noErrors(SpecsIo.getResource("fixtures/public/run/Arrays2.jmm"), "6");
    }

    @Test
    public void testArraysAndBooleans() {
        var nl = SpecsIo.getNewline();
        noErrors(SpecsIo.getResource("fixtures/public/run/ArraysAndBooleans.jmm"), "1"+nl+"0"+nl+"15"+nl+"0"+nl+"1"+nl+"15");
    }

    @Test
    public void testLocalVar() {
        noErrors(SpecsIo.getResource("fixtures/public/run/LocalVar.jmm"), "5");
    }

    @Test
    public void testWhileStmt() {
        noErrors(SpecsIo.getResource("fixtures/public/run/WhileStmt.jmm"), "10");
    }

    @Test
    public void testWhileStmt2() {
        noErrors(SpecsIo.getResource("fixtures/public/run/WhileStmt2.jmm"), "10");
    }

    // ----------

    public void assertNoErrorsGeneral(String filename) {
        noErrors(SpecsIo.getResource("fixtures/public/general/" + filename + ".jmm"),
                SpecsIo.getResource("fixtures/public/general/" + filename + ".txt"));
    }

    @Test
    public void testTicTacToe() {
        assertNoErrorsGeneral("TicTacToe");
    }

    @Test
    public void testWhileAndIf() {
        assertNoErrorsGeneral("WhileAndIf");
    }
}
