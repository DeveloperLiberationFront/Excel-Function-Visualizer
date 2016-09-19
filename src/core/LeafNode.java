package core;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class LeafNode extends Node {
  @Expose
  private String function;
  
  @Expose
  private Example example = null;
  
  @Expose
  private ArrayList<Example> allExamples = new ArrayList<Example>();

  public LeafNode(String func) {
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
    if (example == null || example.getFormulaLength() > newExample.getFormulaLength()) {
      example = newExample;
    }
    
    allExamples.add(newExample);
  }

  @Override
  public void setChildren() {
    // Nothing    
  }
  
  public static final Node[] NO_CHILDREN = {};
  
  public Node[] getChildren() {
    return NO_CHILDREN;
  }

  @Override
  public String toString() {
    return function;
  }
}