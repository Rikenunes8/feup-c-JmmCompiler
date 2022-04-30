package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.jasmin.JasminUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Backend implements JasminBackend {

    private final List<Report> reports = new ArrayList<>();
    private int labelNumber = 0;

    private int newLabel() {
        return labelNumber++;
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        StringBuilder jasminCode = new StringBuilder();
        ClassUnit ollirClass = ollirResult.getOllirClass();

        try {
            jasminCode.append(this.importsToJasmin(ollirClass.getImports()));
            jasminCode.append(this.classDeclarationToJasmin(ollirClass));
            jasminCode.append(this.classMethodsToJasmin(ollirClass));

            // program must output the jasmin code : name <class_name>.j (according to class declaration not input file)
            // Utils.saveFile(this.className + ".j", "generated/jasmin", jasminCode.toString());

            if (!Files.exists(Paths.get("generated/jasmin"))) new File("generated/jasmin").mkdir();
            // FILE aqui tem que ser o que tem o conteudo?...
            JasminUtils.assemble(new File("generated/jasmin/" + ollirClass.getClassName() + ".j"), new File("generated/jasmin"));


            return new JasminResult(ollirResult, jasminCode.toString(), this.reports);
        } catch (Exception e) {
            e.printStackTrace();
            return new JasminResult(ollirClass.getClassName(), null, Collections.emptyList());
                   // Arrays.asList(StyleReport.newError(Stage.GENERATION, "Exception during Jasmin generation", e)));
        }
    }

    private String importsToJasmin(ArrayList<String> ollirImports) {
        // TODO implement

        return "";
    }

    private String classDeclarationToJasmin(ClassUnit ollirClass) {
        // TODO implement

        //    .class public HelloWorld
        //    .super java/lang/Object [se not defined]
        //
        //    ;
        //    ; standard initializer (calls java.lang.Object's initializer)  [coments]
        //    ;
        //    .method public <init>()V  [buid int]
        //       aload_0
        //       invokenonvirtual java/lang/Object/<init>()V  [???]
        //       return
        //    .end method

        return "";
    }

    private String classMethodsToJasmin(ClassUnit ollirClass) {
        // TODO implement

        // construct jasmin code for:
        // 1. function calls*
        // 2. arithmetic expression
        // 3. if the else commands
        // 4. assignments*
        // 5. command sequences*

        return "";
    }
}

