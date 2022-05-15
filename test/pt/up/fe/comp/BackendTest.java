package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;
import static org.junit.Assert.assertEquals;

public class BackendTest {

     @Test
     public void testHelloWorldRunFromJmmFile() {
         var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/general/HelloWorld.jmm"));
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

}