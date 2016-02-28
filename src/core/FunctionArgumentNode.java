package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

/**
 * Basically a wrapper for the HashMap that previously represented an argument in the FunctionStatsNode
 * and all of its different possibilities.
 * @author dlf
 *
 */
public class FunctionArgumentNode {
  @Expose
  private int argumentPosition;
  
  @Expose
  private int positionFrequency = 0;  //TODO: The way this freq differs from FunctionStatsNode is very slight
                                      //      and could probably eventually cause a bug. (FSN increments first
                                      //      in constructor because it adds all children there. This doesn't add
                                      //      anything in constructor, only in `add` and `get`
  //Not exposed.
  private HashMap<String, FunctionStatsNode> possibleArguments_unsorted = new HashMap<String, FunctionStatsNode>();
  
  @Expose
  private ArrayList<FunctionStatsNode> possibleArguments = null;
  
  public FunctionArgumentNode(int pos) {
    this.argumentPosition = pos;
  }

  public void add(FormulaToken child) {
    increment();
    possibleArguments_unsorted.put(child.toSimpleString(), new FunctionStatsNode(child));
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
    return ++positionFrequency;
  }

  public ArrayList<FunctionStatsNode> getPossibilities() {
    return new ArrayList<FunctionStatsNode>(possibleArguments_unsorted.values());
  }
  
  public void sortArgumentsByFrequency() {
    possibleArguments = new ArrayList<FunctionStatsNode>(possibleArguments_unsorted.values());
    Collections.sort(possibleArguments);
    
    for (FunctionStatsNode arg : possibleArguments) 
      arg.sortArgumentsByFrequency();
  }
}