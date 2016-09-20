package core;

import com.google.gson.annotations.Expose;

/**QuantityOfArgumentsNode*/
class QOANode extends Node {
  @Expose
  private int qoa;
  
  @Expose
  private String example = null;

  public QOANode(int quantity) {
    this.qoa = quantity;
    this.children = new Node[quantity];
    for (int i = 0; i < quantity; ++i) {
      this.children[i] = new PositionNode(i + 1);
    }
  }

  @Override
  public void add(Token token, Example newExample) {
    Token[] children = token.getChildren();
    
    increment();
    for (int i = 0; i < children.length; ++i) {
      PositionNode position = (PositionNode) this.children[i];
      Token child = children[i];
      position.add(child, newExample); 
    }
    
    //setExampleIfBetter
    if (example == null || example.length() > newExample.getFormulaLength()) {
      example = newExample.getFormula();
    }
  }

  @Override
  public String toString() {
    return qoa + "";
  }
}