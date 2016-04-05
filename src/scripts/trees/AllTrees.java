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
import core.FunctionNode;
import core.Parser;
import utils.DBUtils;

public class AllTrees {
  public static void main(String[] args) throws SQLException, IOException {
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
    
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation();
    Gson gson = builder.create();
    
    ArrayList<String> list = new ArrayList<String>();
    for (String func : allFuncs.keySet()) {
      String fileFunc;
      if (func.equals("/")) fileFunc="DIV";
      else if (func.equals("*")) fileFunc="MUL";
      else if (func.equals(" ")) fileFunc="SPACE";
      else if (func.equals("()")) fileFunc="PAREN";
      else if (func.equals("=")) fileFunc="EQUALS";
      else if (func.equals("&")) fileFunc="AMP";
      else fileFunc = func;
      
      //else func = func.replace("()", "");
      BufferedWriter write = new BufferedWriter(new FileWriter("./src/viz/json/j" + fileFunc + ".json"));
      write.write(gson.toJson(allFuncs.get(func)));
      write.close();
      
      list.add(fileFunc);
    }
    
    Collections.sort(list);
    BufferedWriter write = new BufferedWriter(new FileWriter("./src/viz/json/index.json"));
    write.write(gson.toJson(list));
    write.close();    
  }
}