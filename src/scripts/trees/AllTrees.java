package scripts.trees;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import core.FormulaToken;
import core.FunctionStatsNode;
import core.Parser;
import utils.DBUtils;

public class AllTrees {
  public static void main(String[] args) throws SQLException, IOException {
    int limit = 100000, offset = 0, currentlyAt;
    Connection con = DBUtils.connectToDatabase();
    PreparedStatement ps = con.prepareStatement("SELECT * FROM formulas_unique WHERE ID > ? LIMIT " + limit + ";");
    
    Map<String, FunctionStatsNode> trees = new HashMap<String, FunctionStatsNode>();
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
          trees.put(toplevel, new FunctionStatsNode(tree.toSimpleString()));
        trees.get(toplevel).add(id, tree);
        
        System.out.println(currentlyAt + " : " + formula);        
      }
      
      rs.previous();
      offset = rs.getInt(1);
      long end = System.currentTimeMillis() - start;
      System.out.println("At " + offset + "... (" + (end/1000.) + "sec)");
    } while (limit == currentlyAt);
    
    ArrayList<FunctionStatsNode> sort = new ArrayList<FunctionStatsNode>(trees.values());
    Collections.sort(sort);
    
    LinkedHashMap<String, FunctionStatsNode> allFuncs = new LinkedHashMap<String, FunctionStatsNode>();
    for (FunctionStatsNode node : sort) {
      allFuncs.put(node.getFunction(), node);
      node.setChildren();
    }
    
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation();
    Gson gson = builder.create();
    
    BufferedWriter write = new BufferedWriter(new FileWriter("./src/viz/all.json"));
    write.write(gson.toJson(allFuncs));
    //System.out.println(gson.toJson(sum));
    write.close();

  }
}