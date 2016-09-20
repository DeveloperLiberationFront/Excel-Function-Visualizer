package core;

import com.google.gson.annotations.Expose;

public abstract class Node implements Comparable<Node> {
  @Expose
  protected int frequency = 0;  
  
  public abstract void add(Token token, Example example);
  
  @Expose
  protected Node[] children = null;  //because it preserves insertion order

  public void setChildren() {
    for (Node child : children) {
      child.setChildren();
    }
  }
  
  public Node[] getChildren() {
    if (children == null) {
      setChildren();
    }
    
    return children;
  }

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
  public int compareTo(Node ob) {
    return ob.getFrequency() - this.frequency;
  }
}