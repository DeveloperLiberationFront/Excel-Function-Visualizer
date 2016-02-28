package scripts.trees;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.ss.formula.FormulaParsingWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import core.FormulaToken;
import core.FunctionStatsNode;
import core.Parser;
import utils.DBUtils;
import utils.POIUtils;

public class SUMTree {
  public static void main(String[] args) throws SQLException, IOException {
    int limit = 100000, offset = 0, currentlyAt;
    Connection con = DBUtils.connectToDatabase();
    PreparedStatement ps = con.prepareStatement("SELECT * FROM formulas WHERE formula like 'SUM%' and ID > ? LIMIT " + limit + ";");
    
    FunctionStatsNode sum = null;
    do {
      currentlyAt = 0;
      ps.setInt(1, offset);
      ResultSet rs = ps.executeQuery();
      
      while (rs.next()) {
        ++currentlyAt;
        String formula = rs.getString(2);
        int sheet = rs.getInt(5);
        
        FormulaToken tree = null;
        try {
          tree = Parser.parseFormula(formula, sheet, Parser.BLANK);
        } catch (Exception e) {
          continue;
        }
        
        if (!tree.toSimpleString().equals("SUM()"))
          continue;
        
        if (sum == null)
          sum = new FunctionStatsNode(tree);
        else
          sum.add(tree);
        
        System.out.println(currentlyAt + " : " + formula);        
      }
      
      rs.previous();
      offset = rs.getInt(1);
      System.out.println("At " + offset + "...");
    } while (limit == currentlyAt);
    
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting().serializeNulls();
    Gson gson = builder.create();
    
    BufferedWriter write = new BufferedWriter(new FileWriter("testJson.txt"));
    write.write(gson.toJson(sum));
    System.out.println(gson.toJson(sum));
    write.close();

  }
}