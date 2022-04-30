

package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class BackendTest {

    private static void noErrors(OllirResult ollirResult) {
        var result = TestUtils.backend(ollirResult);
        TestUtils.noErrors(result.getReports());
        result.compile();
    }

    private static void noErrors(String jmmCode) {
        var result = TestUtils.backend(jmmCode);
        TestUtils.noErrors(result.getReports());
    }

    /*
     * Ollir code that must be successfully parsed
     */

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
