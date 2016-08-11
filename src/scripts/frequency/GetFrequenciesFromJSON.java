package scripts.frequency;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import core.ArgumentNode;
import core.FormulaToken;
import core.FunctionNode;
import core.Node;
import core.Parser;
import utils.DBUtils;

public class GetFrequenciesFromJSON {
  public static void main(String[] args) throws SQLException, IOException {
    /////////THE FOLLOWING I'VE JUST COPIED FROM ALLTREES.JAVA/////////
    LinkedHashMap<String, FunctionNode> allFuncs = makeFullJSONs();
    /////////////END OF COPY/////////////////////
    
    /**
     * To get a big JSON object for each of the functions, I'm just going
     * to piggyback off of the code I've already written. After making objects
     * for all of the individual functions, I want to...
     *    -Reduce those trees to only Function name, Frequency, Children
     *    -Change raw frequency to relative frequency (%)
     *    -Store all in one JSON file.
     * So for this, I will define a new object to be used only in this file.
     */
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation();
    Gson gson = builder.create();
    
    ArrayList<Frequencies> list = new ArrayList<Frequencies>();
    for (String func : allFuncs.keySet()) {
      list.add(new Frequencies(allFuncs.get(func)));
    }
    
    Collections.sort(list);
    BufferedWriter write = new BufferedWriter(new FileWriter("./src/scripts/frequency/frequencies.json"));
    write.write(gson.toJson(list));
    write.close();    
  }
  
  private static class Frequencies implements Comparable<Frequencies> {
    @Expose
    private String func;
    
    @Expose
    private int frequency;
    
    @Expose
    private ArrayList<Frequencies[]> arguments = new ArrayList<Frequencies[]>();
    
    public Frequencies(Node n) {
      func = n.toString();  //Should either be FunctionNode or LeafNode -- for
                            //both, toString should return function name or type.
      frequency = n.getFrequency();
      
      for (Node child : n.getChildren()) {
        Node[] childChild = child.getChildren();
                
        if (childChild == null)
          continue;
        
        Frequencies[] freq = new Frequencies[childChild.length];
        for (int i = 0; i < childChild.length; ++i)
          freq[i] = new Frequencies(childChild[i]);
        
        Arrays.sort(freq);
        arguments.add(freq);
      }
    }

    @Override
    public int compareTo(Frequencies other) {
      return other.getFrequency() - frequency;
    }

    public int getFrequency() {
      return frequency;
    }
  }
  
  //REMEMBER: THIS WAS A COPY FROM ALLTREES.JAVA
  private static LinkedHashMap<String, FunctionNode> makeFullJSONs() throws SQLException {
    int limit = 100000, offset = 0, currentlyAt;
    Connection con = DBUtils.connectToDatabase();
    PreparedStatement ps = con.prepareStatement("SELECT * FROM old_formulas_unique WHERE ID > ? LIMIT " + limit + ";");
    
    Map<String, FunctionNode> trees = new HashMap<String, FunctionNode>();
    do {
      long start = System.currentTimeMillis();
      currentlyAt = 0;
      ps.setInt(1, offset);
      ResultSet rs = ps.executeQuery();
      
      while (rs.next()) {
        ++currentlyAt;
        int id = rs.getInt(1);
        String formula = rs.getString(2);
        int sheet = rs.getInt(5);
        
        FormulaToken tree = null;

        try {
          tree = Parser.parseFormula(formula, Parser.BLANK, sheet);
        } catch (Exception e) {
          System.err.println("ARRRGH");
          continue;
        }
        
        String toplevel = tree.toSimpleString();
        if (!trees.containsKey(toplevel))
          trees.put(toplevel, new FunctionNode(tree.toSimpleString()));
        trees.get(toplevel).add(id, tree);
        
        //if (toplevel.equals("()"))
        //    System.out.println();
        //System.out.println(currentlyAt + " : " + formula);        
      }
      
      rs.previous();
      offset = rs.getInt(1);
      long end = System.currentTimeMillis() - start;
      System.out.println("At " + offset + "... (" + (end/1000.) + "sec)");
    } while (limit == currentlyAt);
    
    ArrayList<FunctionNode> sort = new ArrayList<FunctionNode>(trees.values());
    Collections.sort(sort);
    
    LinkedHashMap<String, FunctionNode> allFuncs = new LinkedHashMap<String, FunctionNode>();
    for (FunctionNode node : sort) {
      allFuncs.put(node.getFunction(), node);
      node.setChildren();
    }
    return allFuncs;
  }
}