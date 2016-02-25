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
      HashMap<String, FunctionStatsNode> argumentPosition = getArgumentPosition(i);
      
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
   * Some functions, like SUM can have a variable number of arguments. This function prevents the case of 
   * trying to access an argument position that hasn't been instantiated yet.
   * 
   * This function works on the assumption that if we need to create a new argument position, it will only
   * ever be one above the current maximum position and never more than one above.
   * 
   * @param i       Position in {@link #arguments} we're trying to access.
   * @return        A HashMap referring to that position.
   */
  private HashMap<String, FunctionStatsNode> getArgumentPosition(int i) {
    HashMap<String, FunctionStatsNode> argumentPosition;
    
    if (i < arguments.size()) {
      argumentPosition = arguments.get(i);
    } else {
      argumentPosition = new HashMap<String, FunctionStatsNode>();
      arguments.add(argumentPosition);
    }
    
    return argumentPosition;
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
      HashMap<String, FunctionStatsNode> argument = getArgumentPosition(i);
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
  
  /**
   * TODO: Why create a new stringbuilder here instead of passing it in like elsewhere?
   * @param depth
   * @return
   */
  protected String tabs(int depth) {
    StringBuilder str = depth % 2 == 0 
                          ? new StringBuilder(depth/2 + ".")
                          : new StringBuilder("..");
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
    return o.getFrequency() - this.frequency;
  }
}