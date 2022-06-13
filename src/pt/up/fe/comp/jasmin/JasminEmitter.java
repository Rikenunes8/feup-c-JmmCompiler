package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Collections;
import java.util.List;

public class JasminEmitter implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        // Table of variables for each method is automatically build
        
        try {
            // check the use of labels in the OLLIR loaded
            ollirClass.checkMethodLabels();

            JasminGenerator translationResult = new JasminGenerator(ollirClass);
            String jasminCode = translationResult.getJasminCode();

            System.out.println("---- Jasmin COde ------"); // TODO remove
            System.out.println(jasminCode);

            return new JasminResult(ollirResult, jasminCode, Collections.emptyList());
        } catch (Exception e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    List.of(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }
    }
}

