package pt.up.fe.comp.ollir;


public class OllirExprPair {
  private String temps;
  private String expression;

  public OllirExprPair() {
    this.temps = "";
    this.expression = "";
  }
  public OllirExprPair(String expression) {
    this.temps = "";
    this.expression = expression;
  }
  public OllirExprPair(String temps, String expression) {
    this.temps = temps;
    this.expression = expression;
  }

  public void setTemps(String temps) {
      this.temps = temps;
  }

  public void setExpression(String expression) {
      this.expression = expression;
  }

  public String getExpression() {
      return expression;
  }

  public String getTemps() {
      return temps;
  }
}