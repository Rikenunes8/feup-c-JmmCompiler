package pt.up.fe.comp.ollir;


public class OllirExprGenerator {
  private final String temps;
  private final String fullExp;
  private final String type;

  public OllirExprGenerator() {
    this.fullExp = "";
    this.type = "";
    this.temps = "";
  }

  public OllirExprGenerator(String fullExp, String type) {
    this.fullExp = fullExp;
    this.type = type;
    this.temps = "";
  }

  public OllirExprGenerator(String fullExp, String type, String temps) {
    this.fullExp = fullExp;
    this.temps = temps;
    this.type = type;
  }

  public String getFullExp() {
      return this.fullExp;
  }
  public String getTemps() {
      return this.temps;
  }
  public String getType() {
    return this.type;
  }
}