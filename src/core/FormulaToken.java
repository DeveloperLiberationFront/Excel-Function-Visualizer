package core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.ptg.Area2DPtgBase;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.BoolPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.formula.ptg.OperandPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.apache.poi.ss.util.CellReference;

import core.Parser.CellContext;

public class FormulaToken {
  protected static Mode mode = Mode.REPLACE;
  protected String tokenStr;
  protected Ptg token;
  private int origLen = Integer.MAX_VALUE;
  
  public enum Mode {
    NO_CHANGE,
    REPLACE,
    R1C1
  }
  
  //TODO: Don't like this blank string possibility, but OperationToken needs a blank super...
  public FormulaToken() {
    this.tokenStr = "";
    this.token = null;
  }
  
  /**
   * Names in a spreadsheet require the spreadsheet context in order to parse correctly.
   * @param token   The Name token.
   * @param render  The contain spreadsheet.
   * @param sheet 
   */
  public FormulaToken(NamePtg token, FormulaRenderingWorkbook render, int sheet) {
    this.token = token;
    this.tokenStr = token.toFormulaString(render).trim();
    
    //TODO: Make unique name class?
    EvaluationName eval = ((FormulaParsingWorkbook) render).getName(tokenStr, sheet);
    Ptg[] toks = eval.getNameDefinition();
    FormulaToken expanded = Parser.parseFormula(toks, render, sheet);
    this.tokenStr = expanded.toString();
  }
  
  /**
   * Creates a class that refers to a discrete token within an Excel formula.
   * ex:  SUM(A:A)+IF(B1<B2, B1, B2) -> SUM(), A:A, +, IF(), B1, <, B2, B1, B2 
   * @param tok   The discrete token in the formula, expected to not be an operation
   *              token and thus have no arguments.
   */
  public FormulaToken(Ptg tok) {
    this(tok, new CellContext(0,0,0));
  }

  public FormulaToken(Ptg tok, CellContext cell) {
    this.token = tok;
    
    switch (mode) {
      case NO_CHANGE:
        this.tokenStr = tok.toFormulaString().trim();   break;
      case REPLACE:
        this.tokenStr = getTypeString(tok);               break;
      case R1C1:
        this.tokenStr = toR1C1String(tok.toFormulaString().trim(), cell);
        break;      
    }
  }

  public static void dontReplace() {
    mode = Mode.NO_CHANGE;
  }
  
  public static void goRelative() {
    mode = Mode.R1C1;
  }
  
  public static void replace() {
    mode = Mode.REPLACE;
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
  
  //(?!\\d)\\$?[A-Z]+\\$?\\d+(?![a-zA-Z]) -> Capture all instance of a set of letters 
  //  followed by a set of numbers. Can't be immediately preceded by another number or 
  //  followed by another letter or an exclamation, as might be indicative in sheet names or strings.
  private static String refPattern = "(?!\\d)\\$?[A-Z]+\\$?\\d+(?![a-zA-Z!])",
                        followedByEvenNumOfQuotes = "(?=([^']*'[^']*')*[^']*$)";
  private static Matcher match = Pattern.compile(refPattern+followedByEvenNumOfQuotes).matcher("");
  public static String toR1C1String(String formula, CellContext cell) {
    int row = cell.getRow() + 1, col = cell.getCol() + 1; //When using POI, cell A1 is 0,0. We need to offset by one.
    StringBuffer newFormula = new StringBuffer();
    
    match.reset(formula);
    while (match.find()) {
      String orig = match.group();
      String[] parts = orig.replaceAll("([A-Z])(\\$?\\d)", "$1 $2").split(" ");
      
      //if (!parts[0].startsWith("$")) {
      String refCol = parts[0];
      boolean absCol = refCol.startsWith("$");
      if (absCol) 
        refCol = refCol.replace("$", "");
      int refColNum = CellReference.convertColStringToIndex(refCol) + 1; //Plus 1 because POI offsets by one.
      refCol = absCol ? "C" + refColNum : "C[" + (refColNum - col) + "]";
      
      String refRow = parts[1];
      boolean absRow = refRow.startsWith("$");
      if (absRow) 
        refRow = refRow.replace("$", "");
      int refRowNum = Integer.parseInt(refRow); 
      refRow = absRow ? "R" + refRowNum : "R[" + (refRowNum - row) + "]";
      
      
      String newRef = refRow + refCol;
      //System.out.println(match.group() + " -> " + newRef + " (" + col + "," + row + ")");
      match.appendReplacement(newFormula, newRef);      
    }      
    
    match.appendTail(newFormula);
    return newFormula.toString();
  }
  
  /**
   * Wrap this token in parenthesis.
   * @return
   */
  public String wrap() {
    if (mode != Mode.REPLACE)
      this.tokenStr = "(" + tokenStr + ")";   //Don't want to wrap a single leaf node for viz purposes.
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
    
    if (o instanceof FunctionNode) {
      FunctionNode fsn = (FunctionNode) o;
      return this.tokenStr.equals(fsn.getFunction());
    } else if (o instanceof FormulaToken) {
      FormulaToken ft = (FormulaToken) o;
      return toSimpleString().equals(ft.toSimpleString());
        //Because simple string differs between FormulaToken and OperationToken, call toSimpleString() instead of tokenStr.
    } 
    
    return false;
  }
}