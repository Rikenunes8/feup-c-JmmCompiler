package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;

public class OptimizationTest {
    @Test
    public void test() {
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/general/HelloWorld.jmm"));
        TestUtils.noErrors(ollirResult.getReports());
    }
}
