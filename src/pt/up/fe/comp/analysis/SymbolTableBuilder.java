package pt.up.fe.comp.analysis;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

import java.util.*;

public class SymbolTableBuilder implements SymbolTable {
 
    List<String> imports;

    String className;
    String superClass;
    List<Symbol> fields; // Class's Private Attibutes

    Map<String, SymbolTableMethod> methods;

    public SymbolTableBuilder() {
      this.imports = new ArrayList<>();
      this.fields  = new ArrayList<>();
      this.className = null;
      this.superClass = null;
      this.methods  = new HashMap<>();
    }
    
    @Override
    public List<String> getImports() {
        return this.imports;
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public String getSuper() {
        return this.superClass;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields;
    }

    @Override
    public List<String> getMethods() {
        return new ArrayList<>(this.methods.keySet());
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return this.methods.get(methodSignature).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return this.methods.get(methodSignature).getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return this.methods.get(methodSignature).getLocalVariables();
    }

    public Boolean getStatic(String methodSignature) {
        return this.methods.get(methodSignature).getStatic();
    }

    public void addImport(String importSignature) {
      this.imports.add(importSignature);
    }

    public void setClassName(String className){
        this.className = className;
    }

    public void setSuper(String superClass){
        this.superClass = superClass;
    }

    public void addField(Symbol field) {
        this.fields.add(field);
    }

    public void addMethod(String methodSignature, Type returnType, List<Symbol> params, List<Symbol> localVariables, Boolean isStatic) {
        this.methods.put(methodSignature, new SymbolTableMethod(methodSignature, returnType, params, localVariables, isStatic));
    }

    public boolean hasMethod(String methodSignature) {
        return this.methods.containsKey(methodSignature);
    }
}