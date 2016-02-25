package scripts.trees;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.ss.formula.FormulaParsingWorkbook;

import core.FormulaToken;
import core.FunctionStatsNode;
import core.Parser;
import utils.DBUtils;
import utils.POIUtils;

public class SUMTree {
  public static void main(String[] args) throws SQLException {
    int limit = 20000, offset = 0, currentlyAt;
    Connection con = DBUtils.connectToDatabase();
    PreparedStatement ps = con.prepareStatement("SELECT * FROM formulas WHERE formula like 'SUM%' LIMIT " + limit + " OFFSET ?");
    
    FunctionStatsNode sum = null;
    do {
      currentlyAt = 0;
      ps.setInt(1, offset);
      ResultSet rs = ps.executeQuery();
      
      while (rs.next()) {
        String formula = rs.getString(2), file = rs.getString(4);
        int sheet = rs.getInt(5);
        FormulaParsingWorkbook parse = POIUtils.getParser(POIUtils.getWorkbook(file));
        
        FormulaToken tree = Parser.parseFormula(formula, sheet, parse);
        
        if (sum == null)
          sum = new FunctionStatsNode(tree);
        else
          sum.add(tree);
        
        System.out.println(formula);
        System.out.println(sum.toString());
        
        ++currentlyAt;
      }
      
      offset += limit;
      break;
    } while (limit == currentlyAt);
  }
}