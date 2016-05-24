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
public class ArgumentNode extends Node {
  @Expose
  private int position;
  
  private Map<String, Node> possibleArguments_unsorted = new HashMap<String, Node>();
  
  @Expose
  private Node[] children = null;  //because it preserves insertion order
  
  public ArgumentNode(int pos) {
    this.position = pos;
  }
  
  public void add(int ex, FormulaToken child) {
    String func = child.toSimpleString();
    increment();

    Node statsNode;
    if (possibleArguments_unsorted.containsKey(func)) {
      statsNode = possibleArguments_unsorted.get(func);
    } else {
      statsNode = isLeafToken(func) ? new LeafNode(func) : new FunctionNode(func);
      possibleArguments_unsorted.put(child.toSimpleString(), statsNode);
    }
    
    statsNode.add(ex, child);
  }
  
  private boolean isLeafToken(String tok) {
    return tok.startsWith("~") && tok.endsWith("~");
  }

  public ArrayList<Node> getPossibilities() {
    return new ArrayList<Node>(possibleArguments_unsorted.values());
  }
  
  public void setChildren() {
    children = possibleArguments_unsorted.values().stream().toArray(Node[]::new); //new FunctionStatsNode[sort.size()]; 
    for (Node node : children)
      node.setChildren();
  }
  
  @Override
  public Node[] getChildren() {
    if (children == null)
      setChildren();
    
    return children;
  }
  
  @Override
  public String toString() {
    return position + "";
  }
}