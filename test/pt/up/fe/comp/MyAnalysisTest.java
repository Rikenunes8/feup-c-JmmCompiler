package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;

public class MyAnalysisTest {
    private static void noErrors(String code) {
        var result = TestUtils.analyse(code);
        TestUtils.noErrors(result);
    }

    private static void mustFail(String code) {
        var result = TestUtils.analyse(code);
        TestUtils.mustFail(result);
        for (var report : result.getReports())
            System.out.println(report);
    }

    /*
     * Code that must be successfully analysed
     */


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
    @Test
    public void method_this() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/method_this.jmm"));
    }
    @Test
    public void call_non_static_method_from_parent() {
        noErrors(SpecsIo.getResource("fixtures/public/analysis/success/call_non_static_method_from_parent.jmm"));
    }

    /*
     * Code that must fail
     */

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
    @Test
    public void true_call_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/true_call.jmm"));
    }
    @Test
    public void main_this_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/main_this.jmm"));
    }
    @Test
    public void call_non_static_method_from_class_f() {
        mustFail(SpecsIo.getResource("fixtures/public/analysis/fail/call_non_static_method_from_class.jmm"));
    }
}
