package core;

import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.Ptg;

public class FormulaToken {
  protected String tokenStr;
  
  //TODO: Don't like this blank string possibility, but OperationToken needs a blank super...
  public FormulaToken() {
    this.tokenStr = "";
  }
  
  public FormulaToken(String token) {
    this.tokenStr = token.trim();
  }
  
  //TODO: Type checking for the Ptg?
  public FormulaToken(Ptg token) {
    this.tokenStr = token.toFormulaString().trim();
  }
  
  public FormulaToken(NamePtg token, FormulaRenderingWorkbook render) {
    this.tokenStr = token.toFormulaString(render).trim();
  }
  
  /**
   * Wrap this token in parenthesis.
   * @return
   */
  public String wrap() {
    this.tokenStr = "(" + tokenStr + ")";
    return tokenStr;
  }
  
  public String toString() {
    return tokenStr;
  }
  
  public String toTreeString() {
    return this.toTreeString(new StringBuilder(), 0).toString();
  }
  
  protected StringBuilder toTreeString(StringBuilder sb, int depth) {    
    sb.append(tabs(depth));
    sb.append(this.tokenStr);
    sb.append("\n");
    
    return sb;
  }

  protected String tabs(int depth) {
    StringBuilder str = new StringBuilder(depth + ".");
    for (int i = 0; i < depth; ++i) {
      str.append("....");
    }
    return str.toString();
  }
}