package pt.up.fe.comp.mytests;
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class MyOptimizationTest {

    @Test
    public void testHelloWorld() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/general/HelloWorld.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void testFunctionAsArgument() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/general/FunctionArgs.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testIfStmt() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/general/IfStmt.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testWhileAndIf() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/general/WhileAndIf.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void temp() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/temp.jmm"));
        TestUtils.noErrors(result.getReports());
    }


    @Test
    public void assignment1() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/assignment1.jmm"));
        TestUtils.noErrors(result.getReports());
    }


    @Test
    public void if_invokevirtual_obj_boolean_cond() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/if_invokevirtual_obj_boolean_cond.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void if_invokevirtual_obj_int_cond() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/if_invokevirtual_obj_int_cond.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void if_invokevirtual_this_boolean_cond() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/if_invokevirtual_this_boolean_cond.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void if_invokevirtual_this_int_cond() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/if_invokevirtual_this_int_cond.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void invokevirtual_obj_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/invokevirtual_obj_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void invokevirtual_this_arg_arr_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/invokevirtual_this_arg_arr_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void invokevirtual_this_arg_arraccess_local() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/invokevirtual_this_arg_arraccess_local.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void invokevirtual_this_arg_arraccess_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/invokevirtual_this_arg_arraccess_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void invokevirtual_this_arg_boolean_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/invokevirtual_this_arg_boolean_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void invokevirtual_this_arg_int_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/invokevirtual_this_arg_int_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void invokevirtual_this_arg_obj_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/invokevirtual_this_arg_obj_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }



    @Test
    public void return_obj_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_obj_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_obj_local() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_obj_local.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_obj_field() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_obj_field.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_int_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_int_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_int_local() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_int_local.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_int_field() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_int_field.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_boolean_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_boolean_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_boolean_local() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_boolean_local.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_boolean_field() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_boolean_field.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_arr_param() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_arr_param.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_arr_local() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_arr_local.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    @Test
    public void return_arr_field() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/jmm/return_arr_field.jmm"));
        TestUtils.noErrors(result.getReports());
    }
}
