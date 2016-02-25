package core;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.ValueOperatorPtg;

public class OperationToken extends FormulaToken {
  private FormulaToken[] children;
  private String op;
  
  /**
   * Constructor used primarily for single-arg SUM. We expect only one arg.
   * Vararged, just in case it's needed in a future case.
   * @param token   String representation of the function, including arguments.
   * @param args    Individual FormulaToken arguments.
   */
  public OperationToken(String token, FormulaToken... args) {
    super(token);
    
    this.op = "SUM()";
    addChildren(args.length, args);
  }

  /**
   * @param token   Spreadsheet operation.
   * @param args    All the arguments in the function defined by token.
   */
  public OperationToken(OperationPtg token, FormulaToken[] args) {
    int len = args.length;
    if (token.getNumberOfOperands() != len)
      throw new UnsupportedOperationException("OperationToken: not enough arguments to the operation.");   
    
    String[] sArgs = Arrays.stream(args).map(s -> s.toString()).toArray(String[]::new);
    this.tokenStr = token.toFormulaString(sArgs);
    this.op = extractOp(token, len);
        
    addChildren(len, args);
    this.toString();
  }

  private void addChildren(int len, FormulaToken... args) {
    children = new FormulaToken[len];
    for (int i = 0; i < len; ++i)
      children[i] = args[i];
  }
  
  private static final Matcher NULL = Pattern.compile("null,?").matcher("");
  private String extractOp(OperationPtg func, int len) {
    String[] nulls = new String[len];
    String funcOp = func.toFormulaString(nulls);
    funcOp = NULL.reset(funcOp).replaceAll("");    
    return funcOp;    
  }
   
  public FormulaToken[] getChildren() {
    return children;
  }
  
  /**
   * Using a matcher to recursively replace null ends up in some weirdness,
   * like strings being split in half and duplicated and so on.
   * The current "store whole string and op individually" is a workaround.
   */
  public String toString() {
    return tokenStr;
  }
  
  public StringBuilder toTreeString(StringBuilder sb, int depth) {    
    sb.append(tabs(depth));
    sb.append(this.op);
    sb.append("\n");
    
    for (FormulaToken child : children) {
      child.toTreeString(sb, depth + 1);
    }
    
    return sb;
  }
}