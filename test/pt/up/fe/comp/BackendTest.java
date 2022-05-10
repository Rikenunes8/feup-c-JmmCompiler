package pt.up.fe.comp;

import org.junit.Test;
import org.specs.comp.ollir.CallInstruction;
import org.specs.comp.ollir.CallType;
import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

public class BackendTest {

     @Test
     public void testHelloWorldRunFromJmmFile() {
         var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
         TestUtils.noErrors(result.getReports());
         var output = result.run();
         assertEquals("Hello, World!", output.trim());
     }

    @Test
    public void testHelloWorldRunFromJasminFile() {
        String jasminCode = SpecsIo.getResource("fixtures/public/jasmin/HelloWorld.j");
        var output = TestUtils.runJasmin(jasminCode);
        assertEquals("Hello World!\nHello World Again!\n", SpecsStrings.normalizeFileContents(output));
    }

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
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void testHelloWorld() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void testLazySort() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void testLife() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void testMonteCarloPi() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void testQuickSort() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void testSimple() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void testTicTacToe() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void testWhileAndIf() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }
}