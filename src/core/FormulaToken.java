package core;

import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.BoolPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.formula.ptg.StringPtg;

public class FormulaToken {
  protected String tokenStr;
  protected Ptg token;
  
  //TODO: Don't like this blank string possibility, but OperationToken needs a blank super...
  public FormulaToken() {
    this.tokenStr = "";
    this.token = null;
  }
  
  public FormulaToken(Ptg tok) {
    this.token = tok;
    //this.tokenStr = token.toFormulaString().trim();

    if (tok instanceof RefPtgBase)
      this.tokenStr = "<REF>";
    else if (tok instanceof AreaPtgBase) 
      this.tokenStr = "<AREA>";
    else if (tok instanceof IntPtg || tok instanceof NumberPtg) 
      this.tokenStr = "<NUM>";
    else if (tok instanceof StringPtg)
      this.tokenStr = "<STR>";
    else if (tok instanceof BoolPtg)
      this.tokenStr = "<BOOL>";
    else
      this.tokenStr = "<OTHER>";
  }
  
  public FormulaToken(NamePtg token, FormulaRenderingWorkbook render) {
    this.token = token;
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
  
  /**
   * @return  An empty array; if it's not an operation, it should have no children.
   */
  public FormulaToken[] getChildren() {
    return new FormulaToken[0];
  }
  
  public String toString() {
    return tokenStr;
  }
  
  /**
   * Functionally identical to toString in this class (but not in OperationToken)
   * @return    the function name
   */
  public String toSimpleString() {
    return toString();
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
  
  /**
   * Equality based on function name equality.
   */
  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    
    if (o instanceof FunctionStatsNode) {
      FunctionStatsNode fsn = (FunctionStatsNode) o;
      return this.tokenStr.equals(fsn.getFunction());
    } else if (o instanceof FormulaToken) {
      FormulaToken ft = (FormulaToken) o;
      return toSimpleString().equals(ft.toSimpleString());
        //Because simple string differs between FormulaToken and OperationToken, call toSimpleString() instead of tokenStr.
    } 
    
    return false;
  }
}