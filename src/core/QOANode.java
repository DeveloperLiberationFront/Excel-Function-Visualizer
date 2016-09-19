package core;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

/**QuantityOfArgumentsNode*/
class QOANode extends Node {
  @Expose
  private int qoa;

  @Expose
  private List<PositionNode> children;

  public QOANode(int quantity) {
    this.qoa = quantity;
    this.children = new ArrayList<PositionNode>();
    for (int i = 0; i < quantity; ++i) {
      this.children.add(new PositionNode(i + 1));
    }
  }

  @Override
  public void add(FormulaToken token, Example example) {
    FormulaToken[] children = token.getChildren();
    
    increment();
    for (int i = 0; i < children.length; ++i) {
      PositionNode position = this.children.get(i);
      FormulaToken child = children[i];
      position.add(child, example); 
    }
  }
  
  public void setChildren() {
    for (PositionNode child : children) {
      child.setChildren();
    }
  }

  @Override
  public Node[] getChildren() {
    return (Node[]) children.toArray();
  }

  @Override
  public String toString() {
    return qoa + "";
  }
}