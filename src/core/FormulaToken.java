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
  private int origLen = Integer.MAX_VALUE;
  
  //TODO: Don't like this blank string possibility, but OperationToken needs a blank super...
  public FormulaToken() {
    this.tokenStr = "";
    this.token = null;
  }
  
  /**
   * Creates a class that refers to a discrete token within an Excel formula.
   * ex:  SUM(A:A)+IF(B1<B2, B1, B2) -> SUM(), A:A, +, IF(), B1, <, B2, B1, B2 
   * @param tok   The discrete token in the formula, expected to not be an operation
   *              token and thus have no arguments.
   */
  public FormulaToken(Ptg tok) {
    this.token = tok;
    //this.tokenStr = token.toFormulaString().trim();

    this.tokenStr = getTypeString(tok);
  }

  /**
   * Replaces a specific basic type (reference, range, int, string, bool) with a generic
   * string representing it so it can be equated with all other FormulaTokens of the 
   * same type.
   * 
   * @param tok   The same type.
   * @return      A generic string representation for that type.
   */
  private String getTypeString(Ptg tok) {
    String type = "";
    
    if (tok instanceof RefPtgBase)                                //A1
      type = "~REF~";
    else if (tok instanceof AreaPtgBase)                          //A1:A10
      type = "~RANGE~";
    else if (tok instanceof IntPtg || tok instanceof NumberPtg)   //1 or 1.0
      type = "~NUM~";
    else if (tok instanceof StringPtg)                            //"str"
      type = "~STR~";
    else if (tok instanceof BoolPtg)                              //TRUE
      type = "~BOOL~";
    else                                                          //errors, for example
      type = "~OTHER~";
    
    return type;
  }
  
  /**
   * Names in a spreadsheet require the spreadsheet context in order to parse correctly.
   * @param token   The Name token.
   * @param render  The contain spreadsheet.
   */
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
  
  /**
   * This is to store the length of the original formula turned into tokens. Since the length
   * can differ between how the parser can reconstruct it and what it was originally, I want
   * to store the number with this extra step rather than trying to recalculate.
   * 
   * Mainly so I can pick out the shortest example from the database to display.
   * @param origLen   The length in characters of the original formula.
   */
  public void setOrigLen(int origLen) {
    this.origLen = origLen;
    for (FormulaToken child : getChildren())
      child.setOrigLen(origLen);
  }
  
  public int getOrigLen() {
    return origLen;
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
  
  /**
   * Get a string which conveys the hierarchical nature of the formula.
   * @return
   */
  public String toTreeString() {
    return this.toTreeString(new StringBuilder(), 0).toString();
  }
  
  /**
   * Represent the entire hierarchy of the formula as indented list.
   * @param sb      The stringbuilder which is passed between formula tokens
   *                and compiles the entire string.
   * @param depth   How many levels deep we are into the hierarchy. Determines tabbing.
   * @return        The StringBuilder passed in, now altered.
   */
  protected StringBuilder toTreeString(StringBuilder sb, int depth) {    
    tabs(sb, depth);
    sb.append(this.tokenStr);
    sb.append("\n");
    
    return sb;
  }

  /**
   * Adds the correct tabbing for an element of this depth.
   * @param sb      The stringbuilder compiling all parts of the tree string representation.
   * @param depth   How many levels down into the tree we are right now.
   */
  protected void tabs(StringBuilder sb, int depth) {
    sb.append(depth + ".");
    for (int i = 0; i < depth; ++i) {
      sb.append("....");
    }
  }
  
  /**
   * Equality based on function name equality. Can be compared to either FormulaToken or FormulaStatsNode.
   * TODO: Is this double equality dangerous?
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