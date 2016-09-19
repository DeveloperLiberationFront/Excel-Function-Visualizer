package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

/**
 * Basically a wrapper for the HashMap that previously represented an argument in the FunctionStatsNode
 * and all of its different possibilities.
 * @author dlf
 *
 */
public class PositionNode extends Node {
  @Expose
  private int position;
  
  private Map<String, Node> childrenMap = new HashMap<String, Node>();
  
  @Expose
  private Node[] children = null;  //because it preserves insertion order
  
  public PositionNode(int pos) {
    this.position = pos;
  }
  
  public void add(FormulaToken child, Example example) {
    String func = child.toSimpleString();
    increment();

    Node funcNode;
    if (childrenMap.containsKey(func)) {
      funcNode = childrenMap.get(func);
    } else {
      funcNode = isLeafToken(func) ? new LeafNode(func) : new FunctionNode(func);
      childrenMap.put(child.toSimpleString(), funcNode);
    }
    
    funcNode.add(child, example);
  }
  
  private boolean isLeafToken(String tok) {
    return tok.startsWith("~") && tok.endsWith("~");
  }

  public ArrayList<Node> getPossibilities() {
    return new ArrayList<Node>(childrenMap.values());
  }
  
  public void setChildren() {
    children = childrenMap.values().stream().toArray(Node[]::new);
    for (Node node : children) {
      node.setChildren();
    }
  }
  
  @Override
  public Node[] getChildren() {
    if (children == null) {
      setChildren();
    }
    
    return children;
  }
  
  @Override
  public String toString() {
    return position + "";
  }
}