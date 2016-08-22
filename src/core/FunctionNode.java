package core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

public class FunctionNode extends Node {
  @Expose
  private String function;  
  
  @Expose
  private Map<Integer, QuantityOfArgumentsNode> specific_quantities = null;
  
  private Map<Integer, ArgumentNode> all_quantities = new LinkedHashMap<Integer, ArgumentNode>();
    
  @Expose
  private int example;
  
  @Expose
  private ArgumentNode[] children = null;
  
  private int shortestExampleLen = Integer.MAX_VALUE;
  
  //Ensures that it's not a unary/binary operator.
  //TODO: + can be either unary or binary...
  private static Matcher nonvariadicFuncs = Pattern.compile("[\\+\\-\\*/\\^&]").matcher("");
    
  /**
   * Represents a certain type of function or primitive type that can appear in a formula. Stores
   * the number of times that this function has been observed here, and it records every type of 
   * argument that has been passed into a function like this, if any.
   * 
   * <p>NOTE: In a single tree of FunctionStatsNodes, there may be several nodes which represent 
   * the same kind of formula, but they are not the same because they occur in different places in 
   * the tree.
   * 
   * <p>Therefore, a tree which has been generated from the formula `SUM(SUM(A:A), 10)` will have 
   * at least two nodes with the function "SUM()". They are not the same.
   * 
   * @param func The type of formula token that this node wraps.
   */
  public FunctionNode(String func) {    
    this.function = func;
    if (!nonvariadicFuncs.reset(func).matches()) {
      specific_quantities = new LinkedHashMap<Integer, QuantityOfArgumentsNode>();
    }
  }
  
  /**
   * Record the occurrence of whatever type of formula element the parameter `token` is, and then
   * recursively record the occurrences of all of `tokens` children further down in the tree.
   * 
   * <p>Example: If we have the formula IF(A1 < A2, SUM(B1:B10), 0), then we want to say we have 
   * observed 1 more instance of a formula which has IF() as its uppermost function, and then 
   * record that it has the tokens < , SUM(), and 0 ( < NUM > ) one level below that, and so 
   * on.
   * 
   * @param ex 
   * @param token   The type of formula token to record, which should be of the same type as this 
   *                stats node.
   */
  @Override
  public void add(int ex, FormulaToken token) {    
    if (!this.equals(token)) {
      throw new UnsupportedOperationException("Trying to pass a FormulaToken which does not "
          + "refer to the same type of token as the FunctionStatsNode: " + token.toSimpleString() 
          + " vs. " + this.function);
    }
    
    int otherExampleLen = token.getOrigLen();
    if (shortestExampleLen > otherExampleLen) {
      example = ex;
      shortestExampleLen = otherExampleLen;
    }
    
    increment();
    FormulaToken[] children = token.getChildren();
    
    if (specific_quantities != null) {
      QuantityOfArgumentsNode quantityNode = getArgumentQuantityNode(children.length);
      quantityNode.add(ex, token);
    }
    
    for (int i = 0; i < children.length; ++i) {
      FormulaToken child = children[i];
      ArgumentNode argumentPosition = getArgumentAtPosition(i);
      argumentPosition.add(ex, child);
    }
  }

  /**
   * Some functions, like SUM can have a variable number of arguments. This function 
   * prevents the case of trying to access an argument position that hasn't been 
   * instantiated yet. 
   * This function works on the assumption that if we need to create a new argument 
   * position, it will only ever be one above the current maximum position and never 
   * more than one above.
   * 
   * @param pos     Position in {@link #all_quantities} we're trying to access.
   * @return        A HashMap referring to that position.
   */
  private ArgumentNode getArgumentAtPosition(int pos) {
    ArgumentNode argumentPosition;
    
    if (pos < all_quantities.size()) {
      argumentPosition = all_quantities.get(pos);
    } else {
      argumentPosition = new ArgumentNode(pos);
      all_quantities.put(pos, argumentPosition);
    }
    
    return argumentPosition;
  }
  
  /**
   * Like above, but for the quantity nodes.
   * @param size  The number of arguments you're looking for.
   * @return      That premade node, or a new one if this is the first one you've found.
   */
  private QuantityOfArgumentsNode getArgumentQuantityNode(int size) {
    QuantityOfArgumentsNode quantityNode;
    
    if (specific_quantities.containsKey(size)) {
      quantityNode = specific_quantities.get(size);
    } else {
      quantityNode = new QuantityOfArgumentsNode(size);
      specific_quantities.put(size, quantityNode);
    }
    
    return quantityNode;
  }
  
  /**
   * When this function is called, the array "children" should be set to null, while 
   * "all_quantities" is the map used to keep track of the children. We just want to 
   * convert the map to the array. 
   * If this function also has specific quantities, we need to set the children for those nodes too.
   * TODO: There's got to be a better way at representing/explaining optional arguments...
   */
  @Override
  public void setChildren() {
    children = all_quantities.values().stream().toArray(ArgumentNode[]::new);
    
    for (ArgumentNode arg : all_quantities.values()) {
      arg.setChildren();
    }
    
    if (specific_quantities != null) {
      if (specific_quantities.size() > 1) {
        for (QuantityOfArgumentsNode node : specific_quantities.values()) {
          node.setChildren();
        }
      } else {
        specific_quantities = null;
      }
    }
  }
  
  public Node[] getChildren() {
    if (children == null) {
      setChildren();
    }
    
    return children;
  }
  
  public String getFunction() {
    return function;
  }
  
  @Override
  public String toString() {
    return function; 
  }
  
  /**
   * When comparing to FunctionStatsNodes, equality must be exact since distinct
   * FSNs can have the exact same content. (Consider the case for SUM(A:A, 2) + SUM(A:A, 2)
   * and how the two SUM nodes will be constructed.)   * 
   * When comparing to FormulaTokens, equality is based on name.   * 
   * TODO: This double-class equality checking might be dangerous.
   */
  @Override
  public boolean equals(Object ob) {   
    if (ob instanceof FunctionNode) {
      return ob == this;
    } else if (ob instanceof FormulaToken) {
      FormulaToken ft = (FormulaToken) ob;
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
}