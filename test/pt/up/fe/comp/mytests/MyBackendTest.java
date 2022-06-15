package pt.up.fe.comp.mytests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MyBackendTest {

    /*
     * Test instruction parsing from ollir to jasmin
     */

    private static void generatedJasminCodeEqualsTemplate(String filename) {
        var ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/ollir/basics/" + filename + ".ollir"), Collections.emptyMap());
        var result = TestUtils.backend(ollirResult);
        result.compile();

       assertEquals(SpecsStrings.normalizeFileContents(SpecsIo.getResource("fixtures/public/ollir/basics/" + filename + ".template")),
                result.getJasminCode());
    }

    @Test
    public void testOllirToJasminClass() {
        generatedJasminCodeEqualsTemplate("class");
    }

    @Test
    public void testOllirToJasminFields() {
        generatedJasminCodeEqualsTemplate("fields");
    }

    @Test
    public void testOllirToJasminTypes() {
        generatedJasminCodeEqualsTemplate("types");
    }

    @Test
    public void testOllirToJasminInvokes() {
        generatedJasminCodeEqualsTemplate("invokes");
    }

    @Test
    public void testOllirToJasminManageFields() {
        generatedJasminCodeEqualsTemplate("manageFields");
    }

    @Test
    public void testOllirToJasminNewObjectCallInstruction() {
        generatedJasminCodeEqualsTemplate("newObject");
    }

    @Test
    public void testOllirToJasminNewArrayCallInstruction() {
        generatedJasminCodeEqualsTemplate("newArray");
    }

    @Test
    public void testOllirToJasminArrayLength() {
        generatedJasminCodeEqualsTemplate("arrayLength");
    }

    @Test
    public void testOllirToJasminReturn() {
        generatedJasminCodeEqualsTemplate("return");
    }

    @Test
    public void testOllirToJasminGotoAndLabel() {
        generatedJasminCodeEqualsTemplate("gotoAndLabel");
    }

    @Test
    public void testOllirToJasminIfGoto() {
        generatedJasminCodeEqualsTemplate("ifgoto");
    }

    @Test
    public void testOllirToJasminIinc() {
        generatedJasminCodeEqualsTemplate("iinc");
    }

    /*
     * Code example tests that must be successfully parsed
     */

    private static void noErrors(OllirResult ollirResult) {
        var result = TestUtils.backend(ollirResult);
        System.out.println(result.getJasminCode());
        result.compile();
        // result.run();
    }

    private static void noErrors(String jmmCode) {
        var result = TestUtils.backend(jmmCode);
        System.out.println(result.getJasminCode());
        result.compile();
    }

    /*
     * Ollir code that must be successfully parsed
     */

    @Test
    public void testHelloWorldOllir() {
        noErrors(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/HelloWorld.ollir"), Collections.emptyMap()));
    }

    @Test
    public void testFac() {
        noErrors(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/Fac.ollir"), Collections.emptyMap()));
    }

    @Test
    public void testMyClass1() {
        noErrors(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass1.ollir"), Collections.emptyMap()));
    }

    @Test
    public void testMyClass2() {
        noErrors(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass2.ollir"), Collections.emptyMap()));
    }

    @Test
    public void testMyClass3() {
        noErrors(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass3.ollir"), Collections.emptyMap()));
    }

    @Test
    public void testMyClass4() {
        noErrors(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass4.ollir"), Collections.emptyMap()));
    }

    /*
     * Jmm Code that must be successfully parsed
     */

    @Test
    public void testFindMaximum() {
        noErrors(SpecsIo.getResource("fixtures/public/general/FindMaximum.jmm"));
    }

    @Test
    public void testFunctionArgs() {
        noErrors(SpecsIo.getResource("fixtures/public/general/FunctionArgs.jmm"));
    }

    @Test
    public void testHelloWorld() {
        noErrors(SpecsIo.getResource("fixtures/public/general/HelloWorld.jmm"));
    }

    @Test
    public void testIfStmt() {
        noErrors(SpecsIo.getResource("fixtures/public/general/IfStmt.jmm"));
    }

    @Test
    public void testLazySort() {
        noErrors(SpecsIo.getResource("fixtures/public/general/Lazysort.jmm"));
    }

    @Test
    public void testLife() {
        noErrors(SpecsIo.getResource("fixtures/public/general/Life.jmm"));
    }

    @Test
    public void testMonteCarloPi() {
        noErrors(SpecsIo.getResource("fixtures/public/general/MonteCarloPi.jmm"));
    }

    // @Test
    public void testQuickSort() {
        noErrors(SpecsIo.getResource("fixtures/public/general/QuickSort.jmm"));
    }

    @Test
    public void testSimple() {
        noErrors(SpecsIo.getResource("fixtures/public/general/Simple.jmm"));
    }

    @Test
    public void testTicTacToe() {
        noErrors(SpecsIo.getResource("fixtures/public/general/TicTacToe.jmm"));
    }

    @Test
    public void testWhileAndIf() {
        noErrors(SpecsIo.getResource("fixtures/public/general/WhileAndIf.jmm"));
    }
}