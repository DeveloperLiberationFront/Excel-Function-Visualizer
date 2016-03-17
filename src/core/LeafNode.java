package core;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class LeafNode extends Node {
  @Expose
  private String function;
  
  @Expose
  private int example;
  
  @Expose
  private ArrayList<Integer> allExamples = new ArrayList<Integer>();
  
  private int shortestExampleLen = Integer.MAX_VALUE;

  public LeafNode(String func) {
    this.function = func;
  }

  @Override
  public void add(int ex, FormulaToken token) {
    if (!function.equals(token.toString()))
      throw new UnsupportedOperationException("Trying to pass a FormulaToken which does not "
          + "refer to the same type of token as the FunctionStatsNode: " + token.toSimpleString() 
          + " vs. " + this.function);
    
    increment();
    int otherExampleLen = token.getOrigLen();
    if (shortestExampleLen > otherExampleLen) {
      example = ex;
      shortestExampleLen = otherExampleLen;
    }    
    
    allExamples.add(ex);
  }

  @Override
  public void setChildren() {
    // Nothing    
  }

  @Override
  public String toString() {
    return function;
  }

  /*@Override
  public void toTreeString(StringBuilder sb, int i) {
    tabs(sb, i);
    sb.append(function);
    sb.append('\n');
  }*/ 
}