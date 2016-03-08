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
public class FunctionArgumentNode implements Node {
  @Expose
  private int position;
  
  @Expose
  private int frequency = 0;  //TODO: The way this freq differs from FunctionStatsNode is very slight
                                      //      and could probably eventually cause a bug. (FSN increments first
                                      //      in constructor because it adds all children there. This doesn't add
                                      //      anything in constructor, only in `add` and `get`
  
  private Map<String, FunctionStatsNode> possibleArguments_unsorted = new HashMap<String, FunctionStatsNode>();
  
  @Expose
  private Node[] children = null;  //because it preserves insertion order
  
  public FunctionArgumentNode(int pos) {
    this.position = pos;
  }
  
  public void add(int ex, FormulaToken child) {
    String func = child.toSimpleString();
    increment();

    FunctionStatsNode statsNode;
    if (possibleArguments_unsorted.containsKey(func)) {
      statsNode = possibleArguments_unsorted.get(func);
    } else {
      statsNode = new FunctionStatsNode(child.toSimpleString());
      possibleArguments_unsorted.put(child.toSimpleString(), statsNode);
    }
    
    statsNode.add(ex, child);
  }
  
  public int increment() {
    return ++frequency;
  }

  public ArrayList<FunctionStatsNode> getPossibilities() {
    return new ArrayList<FunctionStatsNode>(possibleArguments_unsorted.values());
  }
  
  public void setChildren() {
    children = possibleArguments_unsorted.values().stream().toArray(Node[]::new); //new FunctionStatsNode[sort.size()]; 
    for (Node node : children)
      node.setChildren();
  }

  @Override
  public int getFrequency() {
    return frequency;
  }
}