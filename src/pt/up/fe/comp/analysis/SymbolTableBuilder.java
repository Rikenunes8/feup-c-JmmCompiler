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

    // For each method save their return type, parameters and local variables
    List<String> methods;
    Map<String, Type> methodReturnTypes;
    Map<String, List<Symbol>> methodParameters;
    Map<String, List<Symbol>> methodLocalVariables;
    Map<String, Boolean> methodStatic;

    public SymbolTableBuilder() {
      this.imports = new ArrayList<>();
      this.fields  = new ArrayList<>();
      this.className = null;
      this.superClass = null;
      this.methods  = new ArrayList<>();
      this.methodReturnTypes = new HashMap<>();
      this.methodParameters = new HashMap<>();
      this.methodLocalVariables = new HashMap<>();
      this.methodStatic = new HashMap<>();
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
        return this.methods;
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return this.methodReturnTypes.get(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return this.methodParameters.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return this.methodLocalVariables.get(methodSignature);
    }

    public Boolean getStatic(String methodSignature) {
        return this.methodStatic.get(methodSignature);
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
        this.methods.add(methodSignature);
        this.methodReturnTypes.put(methodSignature, returnType);
        this.methodParameters.put(methodSignature, params);
        this.methodLocalVariables.put(methodSignature, localVariables);
        this.methodStatic.put(methodSignature, isStatic);
    }

    public boolean hasMethod(String methodSignature) {
        return this.methods.contains(methodSignature);
    }
}