package core;

import com.google.gson.annotations.Expose;

public abstract class Node implements Comparable<Node> {
  @Expose
  protected int frequency = 0;  
  
  public abstract void add(int ex, FormulaToken token);

  public abstract void setChildren();

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

  //public abstract void toTreeString(StringBuilder sb, int i);

  /**
   * Adds the appropriate amount of tabbing to an element this deep in the tree
   * @param sb    The stringbuilder which is compiling the string for the entire tree.
   * @param depth How deep we are into the hierarchy right now.
   */
  /*protected void tabs(StringBuilder sb, int depth) {
    sb.append(depth % 2 == 0 
                ? depth/2 + "."
                : "..");
    
    for (int i = 0; i < depth; ++i) {
      sb.append("..");
    }
  }*/
  
  
  /**
   * Things with higher frequency should be prioritized higher.
   */
  @Override
  public int compareTo(Node o) {
    return o.getFrequency() - this.frequency;
  }
}