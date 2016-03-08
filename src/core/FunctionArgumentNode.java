package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

/**
 * Basically a wrapper for the HashMap that previously represented an argument in the FunctionStatsNode
 * and all of its different possibilities.
 * @author dlf
 *
 */
public class FunctionArgumentNode {
  @Expose
  private int position;
  
  @Expose
  private int frequency = 0;  //TODO: The way this freq differs from FunctionStatsNode is very slight
                                      //      and could probably eventually cause a bug. (FSN increments first
                                      //      in constructor because it adds all children there. This doesn't add
                                      //      anything in constructor, only in `add` and `get`
  
  private Map<String, FunctionStatsNode> possibleArguments_unsorted = new HashMap<String, FunctionStatsNode>();
  
  @Expose
  private FunctionStatsNode[] children = null;  //because it preserves insertion order
  
  public FunctionArgumentNode(int pos) {
    this.position = pos;
  }
  
  public void add(int ex, FormulaToken child) {
    increment();
    possibleArguments_unsorted.put(child.toSimpleString(), new FunctionStatsNode(ex, child));
  }

  public boolean contains(FormulaToken child) {
    String funcName = child.toSimpleString();
    return possibleArguments_unsorted.containsKey(funcName);
  }

  public FunctionStatsNode get(FormulaToken child) {
    String childFunc = child.toSimpleString();
    if (!possibleArguments_unsorted.containsKey(childFunc)) 
      throw new UnsupportedOperationException("Tried to retrieve instance of a possible argument "
          + "that hasn't been observed yet.");
    
    increment();
    return possibleArguments_unsorted.get(childFunc);
  }
 
  
  public int increment() {
    return ++frequency;
  }

  public ArrayList<FunctionStatsNode> getPossibilities() {
    return new ArrayList<FunctionStatsNode>(possibleArguments_unsorted.values());
  }
  
  public void sortArgumentsByFrequency() {
    //ArrayList<FunctionStatsNode> sort = new ArrayList<FunctionStatsNode>(possibleArguments_unsorted.values());
    //Collections.sort(sort);

    children = possibleArguments_unsorted.values().stream().toArray(FunctionStatsNode[]::new); //new FunctionStatsNode[sort.size()]; 
    for (FunctionStatsNode node : children)
      node.sortArgumentsByFrequency();
    /*for (int i = 0; i < sort.size(); ++i) {
      FunctionStatsNode arg = sort.get(i);
      possibleArguments[i] = arg;
      arg.sortArgumentsByFrequency();
    }*/
  }
}