package core;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.annotations.Expose;

public class FunctionStatsNode implements Comparable<FunctionStatsNode> {
  @Expose
  private String function;
  
  @Expose
  private int frequency = 0;    
  
  @Expose
  private ArrayList<FunctionArgumentNode> arguments = new ArrayList<FunctionArgumentNode>();
  
  /**
   * Represents a certain type of function or primitive type that can appear in a formula. Stores
   * the number of times that this function has been observed here, and it records every type of argument
   * that has been passed into a function like this, if any.
   * 
   * NOTE: In a single tree of FunctionStatsNodes, there may be several nodes which represent the
   * same kind of formula, but they are not the same because they occur in different places in the
   * tree.
   * 
   * Therefore, a tree which has been generated from the formula `SUM(SUM(A:A), 10)` will have at least
   * two nodes with the function "SUM()". They are not the same.
   * 
   * @param token The type of formula token that this node wraps.
   */
  public FunctionStatsNode(FormulaToken token) {
    function = token.toSimpleString();
    
    increment();    //Construction entails one use.
    FormulaToken[] children = token.getChildren();
    for (int i = 0; i < children.length; ++i) {
      FormulaToken child = children[i];
      FunctionArgumentNode arg = new FunctionArgumentNode(i);
      arg.add(child);//map.put(child.toSimpleString(), new FunctionStatsNode(child));
      arguments.add(arg);
    }
  }
  
  /**
   * Record the occurrence of whatever type of formula element the parameter `token` is, and then
   * recursively record the occurrences of all of `tokens` children further down in the tree.
   * @param token   The type of formula token to record, which should be of the same type as this stats node.
   * 
   * Example: If we have the formula IF(A1<A2, SUM(B1:B10), 0), then we want to say we have observed
   * 1 more instance of a formula which has IF() as its uppermost function, and then record that it has
   * the tokens `<`, SUM(), and 0 (<NUM>) one level below that, and so on.
   */
  public void addChildrenOf(FormulaToken token) {
    if (!this.equals(token))
      throw new UnsupportedOperationException("Trying to pass a FormulaToken which does not "
          + "refer to the same type of token as the FunctionStatsNode: " + token.toSimpleString() 
          + " vs. " + this.function);
    
    increment();
    
    FormulaToken[] children = token.getChildren();
    for (int i = 0; i < children.length; ++i) {
      FormulaToken child = children[i];
      FunctionArgumentNode argumentPosition = getArgumentAtPosition(i);
      
      if (argumentPosition.contains(child)) {
        
        FunctionStatsNode function = argumentPosition.get(child);
        function.addChildrenOf(child);
        
      } else {
        
        argumentPosition.add(child);
        
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
  private FunctionArgumentNode getArgumentAtPosition(int i) {
    FunctionArgumentNode argumentPosition;
    
    if (i < arguments.size()) {
      argumentPosition = arguments.get(i);
    } else {
      argumentPosition = new FunctionArgumentNode(i);
      arguments.add(argumentPosition);
    }
    
    return argumentPosition;
  }
  
  public void sortArgumentsByFrequency() {
    for (FunctionArgumentNode arg : arguments)
      arg.sortArgumentsByFrequency();
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
  
  /**
   * Using {@link #toTreeString(StringBuilder, int)}, this returns a string
   * which conveys the hierarchical nature of the formulas and displays the
   * frequency of each kind of argument.
   */
  public String toString() {
    return toTreeString(new StringBuilder(), 0).toString();
  }
  
  private StringBuilder toTreeString(StringBuilder sb, int depth) {
    tabs(sb, depth);
    sb.append(function + " (" + frequency + ")");
    sb.append("\n");
    
    for (int i = 0; i < arguments.size(); ++i) {
      FunctionArgumentNode argument = getArgumentAtPosition(i);
      tabs(sb, depth+1);
      sb.append("Argument #" + (i+1));
      sb.append("\n");
      
      ArrayList<FunctionStatsNode> funcs = argument.getPossibilities();
      Collections.sort(funcs);
      for (FunctionStatsNode func : funcs) {
        func.toTreeString(sb, depth+2);
      }
    }
    
    return sb;
  }
  
  /**
   * Adds the appropriate amount of tabbing to an element this deep in the tree
   * @param sb    The stringbuilder which is compiling the string for the entire tree.
   * @param depth How deep we are into the hierarchy right now.
   */
  protected void tabs(StringBuilder sb, int depth) {
    sb.append(depth % 2 == 0 
                ? depth/2 + "."
                : "..");
    
    for (int i = 0; i < depth; ++i) {
      sb.append("..");
    }
  }

  /**
   * When comparing to FunctionStatsNodes, equality must be exact since distinct
   * FSNs can have the exact same content. (Consider the case for SUM(A:A, 2) + SUM(A:A, 2)
   * and how the two SUM nodes will be constructed.)
   * 
   * When comparing to FormulaTokens, equality is based on name.
   * 
   * TODO: This double-class equality checking might be dangerous.
   */
  @Override
  public boolean equals(Object o) {   
    if (o instanceof FunctionStatsNode) {
      return o == this;
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