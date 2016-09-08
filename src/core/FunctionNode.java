package core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

public class FunctionNode extends Node {
  @Expose
  private String function;

  private Map<Integer, QOANode> all_quantities = new LinkedHashMap<Integer, QOANode>();

  @Expose
  private int example;

  @Expose
  private QOANode[] children = null;

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
      this.function = func; //TODO
      //specific_quantities = new LinkedHashMap<Integer, QuantityOfArgumentsNode>();
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

    setExampleIfBetter(ex, token);
    increment();
    FormulaToken[] children = token.getChildren();

    QOANode goodsize = getArgumentNodes(children.length);
    goodsize.fill(children);
  }

  private QOANode getArgumentNodes(int numChildren) {
    QOANode goodsize;
    if (all_quantities.containsKey(numChildren)) {
      goodsize = all_quantities.get(numChildren);
    } else {
      goodsize = new QOANode(numChildren);
      all_quantities.put(numChildren, goodsize);
    }
    return goodsize;
  }

  /**QuantityOfArgumentsNode*/
  private class QOANode extends Node {
    @Expose
    private int qoa;

    @Expose
    private List<ArgumentNode> children;

    public QOANode(int quantity) {
      this.qoa = quantity;
      this.children = new ArrayList<ArgumentNode>();
      for (int i = 0; i < quantity; ++i) {
        this.children.add(new ArgumentNode(i + 1));
      }
    }

    public void fill(FormulaToken[] children) {
      increment();
      for (int i = 0; i < children.length; ++i) {
        ArgumentNode position = this.children.get(i);
        FormulaToken child = children[i];
        position.add(0, child); //TODO: 0 is placeholder...
      }
    }

    public void setChildren() {
      for (ArgumentNode child : children) {
        child.setChildren();
      }
    }

    @Override
    public void add(int ex, FormulaToken token) {
      // TODO Auto-generated method stub
    }

    @Override
    public Node[] getChildren() {
      return (Node[]) children.toArray();
    }

    @Override
    public String toString() {
      return qoa + "";
    }
  }

  private void setExampleIfBetter(int ex, FormulaToken token) {
    int otherExampleLen = token.getOrigLen();
    if (shortestExampleLen > otherExampleLen) {
      example = ex;
      shortestExampleLen = otherExampleLen;
    }
  }

  /**
   * When this function is called, the array "children" should be set to null, while
   * "all_quantities" is the map used to keep track of the children. We just want to
   * convert the map to the array.
   * If this function also has specific quantities, we need to set the children for those nodes too.
   */
  @Override
  public void setChildren() {
    children = all_quantities.values().stream().toArray(QOANode[]::new);

    for (QOANode child : children) {
      child.setChildren();
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
