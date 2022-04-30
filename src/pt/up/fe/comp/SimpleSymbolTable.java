package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

import java.util.*;

public class SimpleSymbolTable implements SymbolTable {
 
    List<String> imports;

    String className;
    String superClass;
    List<Symbol> fields; // Class's Private Attibutes

    // For each method save their return type, parameters and local variables
    List<String> methods;
    Map<String, Type> methodReturnTypes;
    Map<String, List<Symbol>> methodParameters;
    Map<String, List<Symbol>> methodLocalVariables;

    public SimpleSymbolTable() {
      this.imports = new ArrayList<>();
      this.fields  = new ArrayList<>();
      this.className = null;
      this.superClass = null;
      this.methods  = new ArrayList<>();
      this.methodReturnTypes = new HashMap<>();
      this.methodParameters = new HashMap<>();
      this.methodLocalVariables = new HashMap<>();
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

    /**
     * Add a import to the list of fully qualified names of imports
     * @param importSignature - fully qualified names of an import
     */
    public void addImport(String importSignature) {
      this.imports.add(importSignature);
    }

    /**
     * Change main name of the class
     * @param className - name of the main class
     */
    public void setClassName(String className){
        this.className = className;
    }

    /**
     * Change the name of the extended class of the class
     * @param superClass - the of the class the main class extends, or null if the class does not extend another class
     */
    public void setSuper(String superClass){
        this.superClass = superClass;
    }

    /**
     * Add a field to the list of Symbols that represent the fields of the class
     * @param field - symbol that represents a field of the class
     */
    public void addField(Symbol field) {
        this.fields.add(field);
    }

    /**
     * Add a entry to the hash map of return type of methods
     * @param methodSignature - name of the method with that return type
     * @param methodReturnType - type of the given method
     */
    public void addMethodType(String methodSignature, Type methodReturnType) {
        this.methodReturnTypes.put(methodSignature, methodReturnType);
    }

    /**
     * Add a entry to the hash map of parameters of methods
     * @param methodSignature - name of the method that contains the parameters
     * @param methodParameters - list of parameters of the given method
     */
    public void addMethodParameters(String methodSignature, List<Symbol> methodParameters) {
        this.methodParameters.put(methodSignature, methodParameters);
    }

    /**
     * Add a entry to the hash map of local variables of methods
     * @param methodSignature - name of the method of the scope of the local variable
     * @param methodLocalVariables - list of local variables declared in the given method
     */
    public void addMethodLocalVariables(String methodSignature, List<Symbol> methodLocalVariables) {
        this.methodLocalVariables.put(methodSignature, methodLocalVariables);
    }

    public void addMethod(String methodSignature, Type returnType, List<Symbol> params, List<Symbol> localVariables) {
        this.methods.add(methodSignature);
        addMethodType(methodSignature, returnType);
        addMethodParameters(methodSignature, params);
        addMethodLocalVariables(methodSignature, localVariables);
    }

    public boolean hasMethod(String methodSignature) {
        return this.methods.contains(methodSignature);
    }
}