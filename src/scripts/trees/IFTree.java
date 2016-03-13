package scripts.trees;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import core.FormulaToken;
import core.FunctionStatsNode;
import core.Node;
import core.Parser;
import utils.DBUtils;

public class IFTree {
  public static void main(String[] args) throws SQLException, IOException {
    int limit = 100000, offset = 0, currentlyAt;
    Connection con = DBUtils.connectToDatabase();
    PreparedStatement ps = con.prepareStatement("SELECT * FROM formulas WHERE formula like 'IF%' and ID > ? LIMIT " + limit + ";");
    
    Node sum = null;
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
          continue;
        }
        
        if (!tree.toSimpleString().equals("IF()"))
          continue;
        else if (formula.equals("IF(BQ17,IF(BP17,SQRT(LN(1+((BU17*AZ17)^2*(EXP(CG17^2*BT17)-1)+(BV17*BA17)^2*(EXP(CH17^2*BT17)-1)+(BW17*BB17)^2*(EXP(CI17^2*BT17)-1)+2*BU17*AZ17*BV17*BA17*(EXP(CG17*CH17*BT17)-1)+2*BV17*BA17*BW17*BB17*(EXP(CH17*CI17*BT17)-1)+2*BW17*BB17*BU17*AZ17*(EXP(CI17*CG17*BT17)-1))/(BY17*BP17)^2)/BT17),0),\"\")"))
          System.out.println();
        
        if (sum == null)
          sum = new FunctionStatsNode(tree.toSimpleString());
        sum.add(id, tree);
        
        System.out.println(currentlyAt + " : " + formula);        
      }
      
      rs.previous();
      offset = rs.getInt(1);
      long end = System.currentTimeMillis() - start;
      System.out.println("At " + offset + "... (" + (end/1000.) + "sec)");      
    } while (limit == currentlyAt);
    
    sum.setChildren();
    
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation();
    Gson gson = builder.create();
    
    BufferedWriter write = new BufferedWriter(new FileWriter("./src/viz/iftree.json"));
    write.write(gson.toJson(sum));
    System.out.println(gson.toJson(sum));
    write.close();

  }
}