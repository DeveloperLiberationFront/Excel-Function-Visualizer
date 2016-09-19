package core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

public class FunctionNode extends Node {
  @Expose
  private String function;

  private Map<Integer, QOANode> all_quantities = new LinkedHashMap<Integer, QOANode>();

  @Expose
  private Example example = null;

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
    this.children = null;
    this.function = func;
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
  public void add(FormulaToken token, Example newExample) {
    if (!this.equals(token)) {
      throw new UnsupportedOperationException("Trying to pass a FormulaToken which does not "
          + "refer to the same type of token as the FunctionStatsNode: " + token.toSimpleString()
          + " vs. " + this.function);
    }

    //setExampleIfBetter
    if (example == null || example.getFormulaLength() > newExample.getFormulaLength()) {
      example = newExample;
    }
    
    increment();
    QOANode goodsize = getQOANode(token.getChildren().length);
    goodsize.add(token, newExample);
  }

  private QOANode getQOANode(int numChildren) {
    QOANode goodsize;
    
    if (all_quantities.containsKey(numChildren)) {
      goodsize = all_quantities.get(numChildren);
    } else {
      goodsize = new QOANode(numChildren);
      all_quantities.put(numChildren, goodsize);
    }
    
    return goodsize;
  }

  /**
   * When this function is called, the array "children" should be set to null, while
   * "all_quantities" is the map used to keep track of the children. We just want to
   * convert the map to the array.
   * If this function also has specific quantities, we need to set the children for those nodes too.
   */
  @Override
  public void setChildren() {
    children = all_quantities.values().stream().toArray(Node[]::new);
    super.setChildren();
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
   * @return  The hashcode of the string function name.
   */
  @Override
  public int hashCode() {
    return function.hashCode();
  }
}
