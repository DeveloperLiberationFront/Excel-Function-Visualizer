package scripts.filesAndDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import core.Parser;
import utils.DBUtils;

public class HowManyWithBlankWBs {
  public static void main(String[] args) throws SQLException {
    int limit = 50000, at = 0, offset = 0;
    
    Connection con = DBUtils.connectToDatabase();
    PreparedStatement ps = con.prepareStatement("SELECT formula FROM formulas WHERE id > ? LIMIT " + limit + ";");
    
    int failed = 0;
    do {
      at = 0;
      
      ps.setInt(1, offset);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        ++at;
        
        String formula = rs.getString(1);
        try {
          Parser.parseFormula(formula);
        } catch (Exception | Error e) {
          System.out.println(formula);
          ++failed;
        }        
      }
      
      offset += limit;
    } while (at == limit);
    
    System.out.println(failed);
  }
}