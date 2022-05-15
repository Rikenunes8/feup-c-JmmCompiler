package pt.up.fe.comp.mytests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

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
    public void testFunctionCall() {
        noErrors(SpecsIo.getResource("fixtures/public/run/FunctionCall.jmm"), "3");
    }

    @Test
    public void testLocalVar() {
        noErrors(SpecsIo.getResource("fixtures/public/run/LocalVar.jmm"), "5");
    }

    //TODO ifstmt

    //TODO whileloop

    //TODO chamadas a this.funções

    //TODO expressões aritmeticas



}
