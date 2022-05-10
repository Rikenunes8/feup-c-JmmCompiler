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
        
        try {
            // check the use of labels in the OLLIR loaded
            ollirClass.checkMethodLabels();
            // build the table of variables for each method
            ollirClass.buildVarTables();
            ollirClass.show(); // TO DEVELOPMENT PURPOSES

            OllirToJasmin translationResult = new OllirToJasmin(ollirClass);
            String jasminCode = translationResult.getJasminCode();

            return new JasminResult(ollirResult, jasminCode, Collections.emptyList());
        } catch (Exception e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    List.of(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }
    }
}

