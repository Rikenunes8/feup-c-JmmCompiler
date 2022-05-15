package pt.up.fe.comp.mytests;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class MyCompilerTest {

    private static void noErrors(String jmmCode) {
        var result = TestUtils.backend(jmmCode);
        System.out.println(result.getJasminCode());
        result.compile();
        var outPut = result.run();
        System.out.println(outPut);
    }

    @Test
    public void testWhileAndIf() {
        noErrors(SpecsIo.getResource("fixtures/public/run/IfStmt.jmm"));
    }
}
