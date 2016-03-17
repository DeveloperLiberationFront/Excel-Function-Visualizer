package core;

import com.google.gson.annotations.Expose;

public class QuantityOfArgumentsNode extends Node {
  @Expose
  private int quantity;
  
  @Expose
  private ArgumentNode[] children;  //arguments
  
  @Expose
  private int example;
  
  private int shortestExampleLen = Integer.MAX_VALUE;
  
  public QuantityOfArgumentsNode(int quantity) {
    this.quantity = quantity;
    children = new ArgumentNode[quantity];
    for (int i = 0; i < quantity; ++i)
      children[i] = new ArgumentNode(i);
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
      ArgumentNode argumentPosition = children[i];        
      argumentPosition.add(ex, child);
    }
  }
  
  public int increment() {
    return ++frequency;
  }

  public void setChildren() {
    for (ArgumentNode node : children)
      node.setChildren();
  }

  public int getFrequency() {
    return frequency;
  }

  @Override
  public String toString() {
    return quantity + "";
  }
}