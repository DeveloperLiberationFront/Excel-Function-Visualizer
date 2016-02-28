package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.annotations.Expose;

/**
 * Basically a wrapper for the HashMap that previously represented an argument in the FunctionStatsNode
 * and all of its different possibilities.
 * @author dlf
 *
 */
public class FunctionArgumentNode {
  private int argumentPosition;
  
  @Expose
  private int positionFrequency = 0;  //TODO: The way this freq differs from FunctionStatsNode is very slight
                                      //      and could probably eventually cause a bug. (FSN increments first
                                      //      in constructor because it adds all children there. This doesn't add
                                      //      anything in constructor, only in `add` and `get`
  
  private HashMap<String, FunctionStatsNode> possibleArguments_unsorted = new HashMap<String, FunctionStatsNode>();
  
  @Expose
  private LinkedHashMap<String, FunctionStatsNode> possibleArguments = null;
  
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
    ArrayList<FunctionStatsNode> sort = new ArrayList<FunctionStatsNode>(possibleArguments_unsorted.values());
    Collections.sort(sort);

    possibleArguments = new LinkedHashMap<String, FunctionStatsNode>();    
    for (FunctionStatsNode arg : sort) {
      String key = arg.getFunction();     
      possibleArguments.put(key, arg);
      arg.sortArgumentsByFrequency();
    }
  }
}