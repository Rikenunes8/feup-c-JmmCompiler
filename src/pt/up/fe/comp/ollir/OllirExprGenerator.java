package pt.up.fe.comp.ollir;


public class OllirExprGenerator {
  private final String temps;
  private final String fullExp;

  public OllirExprGenerator() {
    this.temps = "";
    this.fullExp = "";
  }

  public OllirExprGenerator(String fullExp) {
    this.fullExp = fullExp;
    this.temps = "";
  }

  public OllirExprGenerator(String fullExp, String temps) {
    this.fullExp = fullExp;
    this.temps = temps;
  }

  public String getFullExp() {
      return this.fullExp;
  }
  public String getTemps() {
      return temps;
  }
}