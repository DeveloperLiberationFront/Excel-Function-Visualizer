package core;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Basically a wrapper for the HashMap that prevously represented an argument in the FunctionStatsNode
 * and all of its different possibilities.
 * @author dlf
 *
 */
public class FunctionArgumentNode {
  private int argumentPosition;
  private HashMap<String, FunctionStatsNode> possibleArguments = new HashMap<String, FunctionStatsNode>();
  
  public FunctionArgumentNode(int pos) {
    this.argumentPosition = pos;
  }

  public void add(FormulaToken child) {
    possibleArguments.put(child.toSimpleString(), new FunctionStatsNode(child));
  }

  public boolean contains(FormulaToken child) {
    String funcName = child.toSimpleString();
    return possibleArguments.containsKey(funcName);
  }

  public FunctionStatsNode get(FormulaToken child) {
    return this.get(child.toSimpleString());
  }
  
  public FunctionStatsNode get(String childFunc) {
    if (!possibleArguments.containsKey(childFunc)) 
      throw new UnsupportedOperationException("Tried to retrieve instance of a possible argument "
          + "that hasn't been observed yet.");
    return possibleArguments.get(childFunc);
  }

  public ArrayList<FunctionStatsNode> getPossibilities() {
    return new ArrayList<FunctionStatsNode>(possibleArguments.values());
  }
}