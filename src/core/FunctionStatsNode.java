package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FunctionStatsNode implements Comparable<FunctionStatsNode> {
  /**
   * ArrayList of HashMaps:         For each argument position, there will be a number of possibilities. The possibilities
   *                                should be ordered by frequency descending, but I will sort them afterward to avoid 
   *                                having to remove/reinsert nodes in a priority queue.
   */
  private String function;
  private ArrayList<HashMap<String, FunctionStatsNode>> arguments = new ArrayList<HashMap<String, FunctionStatsNode>>();
  private int frequency = 1;
  
  /**
   * 
   * @param token
   */
  public FunctionStatsNode(FormulaToken token) {
    function = token.toSimpleString();
    
    FormulaToken[] children = token.getChildren();
    for (FormulaToken child : children) {
      HashMap<String, FunctionStatsNode> map = new HashMap<String, FunctionStatsNode>();
      map.put(child.toSimpleString(), new FunctionStatsNode(child));
      arguments.add(map);
    }
  }
  
  /**
   * 
   * @param token
   */
  public void add(FormulaToken token) {
    increment();
    
    FormulaToken[] children = token.getChildren();
    for (int i = 0; i < children.length; ++i) {
      FormulaToken child = children[i];
      HashMap<String, FunctionStatsNode> argumentPosition = arguments.get(i);
      
      String childFunc = child.toSimpleString();
      if (argumentPosition.containsKey(childFunc)) {
        
        FunctionStatsNode function = argumentPosition.get(childFunc);
        function.add(child);
        
      } else {
        
        argumentPosition.put(childFunc, new FunctionStatsNode(child));
        
      }
    }
  }
  
  /**
   * Record this type of function as being used one more time.
   * @return    New frequency.
   */
  public int increment() {
    return ++frequency;
  }
  
  public String getFunction() {
    return function;
  }
  
  public int getFrequency() {
    return frequency;
  }
  
  public String toString() {
    return toString(new StringBuilder(), 0).toString();
  }
  
  private StringBuilder toString(StringBuilder sb, int depth) {
    sb.append(tabs(depth));
    sb.append(function + " (" + frequency + ")");
    sb.append("\n");
    
    for (int i = 0; i < arguments.size(); ++i) {
      HashMap<String, FunctionStatsNode> argument = arguments.get(i);
      sb.append(tabs(depth+1));
      sb.append("Argument #" + (i+1));
      sb.append("\n");
      
      ArrayList<FunctionStatsNode> funcs = new ArrayList<FunctionStatsNode>(argument.values());
      Collections.sort(funcs);
      for (FunctionStatsNode func : funcs) {
        func.toString(sb, depth+2);
      }
    }
    
    return sb;
  }
  
  protected String tabs(int depth) {
    StringBuilder str = new StringBuilder(depth + ".");
    for (int i = 0; i < depth; ++i) {
      str.append("..");
    }
    return str.toString();
  }

  /**
   * Equality based on function name equality.
   */
  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    
    if (o instanceof FunctionStatsNode) {
      FunctionStatsNode fsn = (FunctionStatsNode) o;
      return this.function.equals(fsn.getFunction());
    } else if (o instanceof FormulaToken) {
      FormulaToken ft = (FormulaToken) o;
      return this.function.equals(ft.toSimpleString());
    } 
    
    return false;
  }
  
  /**
   * Hashcode based on function string.
   * @return
   */
  @Override
  public int hashCode() {
    return function.hashCode();
  }
  
  /**
   * Things with higher frequency should be prioritized higher.
   */
  @Override
  public int compareTo(FunctionStatsNode o) {
    return this.frequency - o.getFrequency();
  }
}