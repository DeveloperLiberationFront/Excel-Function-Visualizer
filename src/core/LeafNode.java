package core;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class LeafNode extends Node {
  @Expose
  private String function;
  
  @Expose
  private String example = null;
  
  @Expose
  private ArrayList<String> allExamples = new ArrayList<String>();

  public static final Node[] NO_CHILDREN = {};
  public LeafNode(String func) {
    this.children = NO_CHILDREN;
    this.function = func;
  }

  @Override
  public void add(FormulaToken token, Example newExample) {
    if (!function.equals(token.toString())) {
      throw new UnsupportedOperationException("Trying to pass a FormulaToken which does not "
          + "refer to the same type of token as the FunctionStatsNode: " + token.toSimpleString() 
          + " vs. " + this.function);
    }
    
    increment();
    
    //setExampleIfBetter
    if (example == null || example.length() > newExample.getFormulaLength()) {
      example = newExample.getFormula();
    }
    
    allExamples.add(newExample.toString());
  }

  @Override
  public String toString() {
    return function;
  }
}