package core;

import com.google.gson.annotations.Expose;

public class QuantityOfArgumentsNode implements Node, Comparable<QuantityOfArgumentsNode> {
  @Expose
  private int quantity;
  
  @Expose
  private int frequency;
  
  @Expose
  private FunctionArgumentNode[] children;  //arguments
  
  @Expose
  private int example;
  
  private int shortestExampleLen = Integer.MAX_VALUE;
  
  public QuantityOfArgumentsNode(int quantity) {
    this.quantity = quantity;
    children = new FunctionArgumentNode[quantity];
    for (int i = 0; i < quantity; ++i)
      children[i] = new FunctionArgumentNode(i);
  }
  
  public void add(int ex, FormulaToken token) {
    increment();
    
    int otherExampleLen = token.getOrigLen();
    if (shortestExampleLen > otherExampleLen) {
      example = ex;
      shortestExampleLen = otherExampleLen;
    }
    
    FormulaToken[] tokChildren = token.getChildren();
    if (tokChildren.length != quantity)
      throw new UnsupportedOperationException("Trying to populate a QOANode with a function holding a different number of args.");
    
    for (int i = 0; i < tokChildren.length; ++i) {
      FormulaToken child = tokChildren[i];
      FunctionArgumentNode argumentPosition = children[i];        
      argumentPosition.add(ex, child);
    }
  }
  
  public int increment() {
    return ++frequency;
  }

  @Override
  public void setChildren() {
    for (FunctionArgumentNode node : children)
      node.setChildren();
  }

  @Override
  public int getFrequency() {
    return frequency;
  }

  @Override
  public int compareTo(QuantityOfArgumentsNode o) {
    return o.getFrequency() - this.frequency;
  }
}