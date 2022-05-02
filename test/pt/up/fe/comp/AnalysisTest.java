package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;

public class AnalysisTest {
    private static void noErrors(String code) {
        var result = TestUtils.analyse(code);
        TestUtils.noErrors(result);
        // System.out.println(result.getSymbolTable().print());
    }

    private static void mustFail(String code) {
        var result = TestUtils.analyse(code);
        TestUtils.mustFail(result);
        for (var report : result.getReports())
            System.out.println(report);

        // System.out.println(result.getSymbolTable().print());
    }

    /*
     * Code that must be successfully analysed
     */

    @Test
    public void helloWorld() {
        noErrors(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
    }

    @Test
    public void findMaximum() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void lazysort() {
        noErrors(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
    }

    @Test
    public void life() {
        noErrors(SpecsIo.getResource("fixtures/public/Life.jmm"));
    }

    @Test
    public void quickSort() {
        // noErrors(SpecsIo.getResource("fixtures/public/QuickSort.jmm")); // TODO Method overloading is not to be implemented
    }

    @Test
    public void simple() {
        noErrors(SpecsIo.getResource("fixtures/public/Simple.jmm"));
    }

    @Test
    public void ticTacToe() {
        noErrors(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
    }

    @Test
    public void whileAndIf() {
        noErrors(SpecsIo.getResource("fixtures/public/WhileAndIf.jmm"));
    }



    @Test
    public void import_super() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/import_super.jmm"));
    }
    @Test
    public void import_type() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/import_type.jmm"));
    }
    @Test
    public void var_declaration1() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/var_declaration1.jmm"));
    }
    @Test
    public void var_declaration2() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/var_declaration2.jmm"));
    }
    @Test
    public void var_declaration_imported_method() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/var_declaration_imported_method.jmm"));
    }
    @Test
    public void var_declaration_method_call() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/var_declaration_method_call.jmm"));
    }
    @Test
    public void params_args() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/params_args.jmm"));
    }
    @Test
    public void undefined_var1() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/undefined_var1.jmm"));
    }
    @Test
    public void undefined_var2() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/undefined_var2.jmm"));
    }
    @Test
    public void undefined_var3() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/undefined_var3.jmm"));
    }
    @Test
    public void undefined_var_array1() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/undefined_var_array1.jmm"));
    }
    @Test
    public void undefined_var_array2() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/undefined_var_array2.jmm"));
    }
    @Test
    public void call_class_object_method() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/call_class_object_method.jmm"));
    }

    /*
     * Code that must fail
     */

    @Test
    public void arr_index_not_int() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/arr_index_not_int.jmm"));
    }

    @Test
    public void arr_size_not_int() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/arr_size_not_int.jmm"));
    }

    @Test
    public void badArguments() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/badArguments.jmm"));
    }

    @Test
    public void binop_incomp() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/binop_incomp.jmm"));
    }

    @Test
    public void funcNotFound() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/funcNotFound.jmm"));
    }

    @Test
    public void simple_length() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/simple_length.jmm"));
    }

    @Test
    public void var_exp_incomp() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/var_exp_incomp.jmm"));
    }

    @Test
    public void var_lit_incomp() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/var_lit_incomp.jmm"));
    }

    @Test
    public void var_undef() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/var_undef.jmm"));
    }

    @Test
    public void varNotInit() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/semantic/varNotInit.jmm"));
    }



    @Test
    public void import_super_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/import_super.jmm"));
    }
    @Test
    public void import_type_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/import_type.jmm"));
    }
    @Test
    public void var_declaration1_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/var_declaration1.jmm"));
    }
    @Test
    public void var_declaration2_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/var_declaration2.jmm"));
    }
    @Test
    public void var_declaration_imported_method_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/var_declaration_imported_method.jmm"));
    }
    @Test
    public void params_args_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/params_args.jmm"));
    }
    @Test
    public void undefined_var1_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/undefined_var1.jmm"));
    }
    @Test
    public void undefined_var2_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/undefined_var2.jmm"));
    }
    @Test
    public void undefined_var3_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/undefined_var3.jmm"));
    }
    @Test
    public void undefined_var_array1_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/undefined_var_array1.jmm"));
    }
    @Test
    public void undefined_var_array2_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/undefined_var_array2.jmm"));
    }

    @Test
    public void call_class_object_method_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/call_class_object_method.jmm"));
    }
}
