package core;

import com.google.gson.annotations.Expose;

/**QuantityOfArgumentsNode*/
class QOANode extends Node {
  @Expose
  private int qoa;

  public QOANode(int quantity) {
    this.qoa = quantity;
    this.children = new Node[quantity];
    for (int i = 0; i < quantity; ++i) {
      this.children[i] = new PositionNode(i + 1);
    }
  }

  @Override
  public void add(FormulaToken token, Example example) {
    FormulaToken[] children = token.getChildren();
    
    increment();
    for (int i = 0; i < children.length; ++i) {
      PositionNode position = (PositionNode) this.children[i];
      FormulaToken child = children[i];
      position.add(child, example); 
    }
  }

  @Override
  public String toString() {
    return qoa + "";
  }
}