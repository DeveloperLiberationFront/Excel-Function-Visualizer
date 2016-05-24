package core;

import com.google.gson.annotations.Expose;

public abstract class Node implements Comparable<Node> {
  @Expose
  protected int frequency = 0;  
  
  public abstract void add(int ex, FormulaToken token);

  public abstract void setChildren();
  
  public abstract Node[] getChildren();

  /**
   * Record this type of function as being used one more time.
   * @return    New frequency.
   */
  public int increment() {
    return ++frequency;
  }
  
  public int getFrequency() {
    return frequency;
  }

  public abstract String toString();  
  
  /**
   * Things with higher frequency should be prioritized higher.
   */
  @Override
  public int compareTo(Node o) {
    return o.getFrequency() - this.frequency;
  }
}