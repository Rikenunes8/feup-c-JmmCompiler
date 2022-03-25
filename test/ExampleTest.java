import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

public class ExampleTest {

    @Test
    public void testExpression() {
        JmmParserResult parserResult;

        parserResult = TestUtils.parse("2+3");
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("0*9");
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("6-3");
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("7/8");
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("2+(3+5)");
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("2+(3*(4/(5-6)))");
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("2++3");
        TestUtils.mustFail(parserResult.getReports());

        // parserResult = TestUtils.parse("2+3\n10+20\n");
        // var parserResult = TestUtils.parse("2+3\n10+20\n");
        // parserResult.getReports().get(0).getException().get().printStackTrace();
        // // System.out.println();
        // var analysisResult = TestUtils.analyse(parserResult);
    }

}
