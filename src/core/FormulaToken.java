package core;

import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.Ptg;

public class FormulaToken {
  protected String token;
  
  //TODO: Don't like this blank string possibility, but OperationToken needs a blank super...
  public FormulaToken() {
    this.token = "";
  }
  
  public FormulaToken(String token) {
    this.token = token.trim();
  }
  
  //TODO: Type checking for the Ptg?
  public FormulaToken(Ptg token) {
    this.token = token.toFormulaString().trim();
  }
  
  public FormulaToken(NamePtg token, FormulaRenderingWorkbook render) {
    this.token = token.toFormulaString(render).trim();
  }
  
  /**
   * Wrap this token in parenthesis.
   * @return
   */
  public String wrap() {
    this.token = "(" + token + ")";
    return token;
  }
  
  public String toString() {
    return token;
  }
  
  public String toTreeString() {
    return this.toTreeString(new StringBuilder(), 0).toString();
  }
  
  protected StringBuilder toTreeString(StringBuilder sb, int depth) {    
    sb.append(tabs(depth));
    sb.append(this.token);
    sb.append("\n");
    
    return sb;
  }

  protected String tabs(int depth) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < depth; ++i) {
      str.append("....");
    }
    return str.toString();
  }
}