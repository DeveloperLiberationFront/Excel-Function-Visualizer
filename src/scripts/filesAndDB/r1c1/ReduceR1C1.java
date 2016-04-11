package scripts.filesAndDB.r1c1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.poi.ss.util.CellReference;

import com.mysql.jdbc.MysqlDataTruncation;

import core.FormulaToken;
import core.Parser;
import utils.DBUtils;
import utils.POIUtils;

public class ReduceR1C1 { 
  public static void main(String[] args) throws SQLException {
    Connection con = DBUtils.connectToDatabase();
    PreparedStatement insert = con.prepareStatement("INSERT INTO formulas_r1c1 "
        + "(id, formula) VALUES (?, ?);");
    PreparedStatement get = con.prepareStatement("SELECT * FROM formulas WHERE file = ? and sheet = ? ORDER BY row, col;");
    ResultSet files = con.createStatement().executeQuery("SELECT DISTINCT file, sheet FROM formulas;");
    
    Parser.goRelative();
    while (files.next()) {
      String file = files.getString(1);   get.setString(1, file);
      int sheet = files.getInt(2);        get.setInt(2, sheet);

      ResultSet formulas = get.executeQuery();
      while (formulas.next()) {
        int id = formulas.getInt(1);        
        String formula = formulas.getString(2);
        int row = formulas.getInt(7), col = CellReference.convertColStringToIndex(formulas.getString(8));
                
        String newFormula;
        try {          
          newFormula = Parser.parseFormula(formula, row, col).toString();
        } catch (UnsupportedOperationException e) {
          System.err.println(e.getMessage());
          System.err.println(id + " : " + formula);
          newFormula = "error";
        }
        
        try {
          insert.setInt(1, id);           insert.setString(2, newFormula);
          insert.executeUpdate();
        } catch (MysqlDataTruncation e) {
          System.err.println(newFormula.length());
          insert.setString(2, "error-toolong");
          insert.executeUpdate();
        }
      }
    }
  }
}