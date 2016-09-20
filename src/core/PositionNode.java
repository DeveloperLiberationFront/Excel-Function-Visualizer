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
  
  public PositionNode(int pos) {
    this.children = null;
    this.position = pos;
  }
  
  public void add(Token child, Example example) {
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
    super.setChildren();
  }
  
  @Override
  public String toString() {
    return position + "";
  }
}