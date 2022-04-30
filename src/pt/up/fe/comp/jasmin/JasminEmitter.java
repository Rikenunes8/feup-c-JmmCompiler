package pt.up.fe.comp.jasmin;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JasminEmitter implements JasminBackend {

    private final List<Report> reports = new ArrayList<>();

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        String jasminCode = new OllirToJasmin(ollirResult.getOllirClass()).getJasminCode();

        return new JasminResult(ollirResult, jasminCode, Collections.emptyList());
    }
}

