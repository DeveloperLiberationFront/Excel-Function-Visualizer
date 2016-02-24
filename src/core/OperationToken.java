package core;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.ss.formula.ptg.OperationPtg;

public class OperationToken extends FormulaToken {
  private ArrayList<FormulaToken> children = new ArrayList<FormulaToken>();
  
  /**
   * Constructor used primarily for single-arg SUM. We expect only one arg.
   * Vararged, just in case it's needed in a future case.
   * @param token   String representation of the function, including arguments.
   * @param args    Individual FormulaToken arguments.
   */
  public OperationToken(String token, FormulaToken... args) {
    super(token);
    
    for (FormulaToken arg : args)
      children.add(arg);
  }
  
  /**
   * @param token   Spreadsheet operation.
   * @param args    All the arguments in the function defined by token.
   */
  public OperationToken(OperationPtg token, FormulaToken[] args) {
    String[] sArgs = Arrays.stream(args).map(s -> s.toString()).toArray(String[]::new);
    this.token = token.toFormulaString(sArgs);
    
    if (token.getNumberOfOperands() != args.length)
      throw new UnsupportedOperationException("OperationToken: not enough arguments to the operation.");   
    
    for (FormulaToken arg : args)
      children.add(arg);
  }
 
  
  public void addChild(FormulaToken child) {
    children.add(child);
  }
  
  public ArrayList<FormulaToken> getChildren() {
    return children;
  }
  
  public StringBuilder toTreeString(StringBuilder sb, int depth) {    
    super.toTreeString(sb, depth);    
    for (FormulaToken child : children) {
      child.toTreeString(sb, depth + 1);
    }
    
    return sb;
  }
}