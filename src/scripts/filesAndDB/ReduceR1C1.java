package scripts.filesAndDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.poi.ss.util.CellReference;

import utils.DBUtils;
import utils.POIUtils;

public class ReduceR1C1 { 
  public static void main(String[] args) throws SQLException {
    Connection con = DBUtils.connectToDatabase();
    PreparedStatement insert = con.prepareStatement("INSERT INTO formulas_unique "
        + "(id, formula, src, file, sheet, sheetName, row, col, usedNames, freq) "
        + "VALUES (?, ?, 'ENRON', ?, ?, ?, ?, ?, ?, 0);");
    PreparedStatement update = con.prepareStatement("UPDATE formulas_unique SET freq = ? WHERE id = ?;");
    PreparedStatement get = con.prepareStatement("SELECT * FROM formulas WHERE file = ? and sheet = ? ORDER BY row, col;");
    ResultSet files = con.createStatement().executeQuery("SELECT DISTINCT file, sheet FROM formulas;");
    
    while (files.next()) {
      String file = files.getString(1);   get.setString(1, file);
      int sheet = files.getInt(2);        get.setInt(2, sheet);
      
      HashMap<String, Integer> seenFreqs = new HashMap<String, Integer>();
      HashMap<String, Integer> seenIds = new HashMap<String, Integer>();
      ResultSet formulas = get.executeQuery();
      while (formulas.next()) {
        int id = formulas.getInt(1);
        String formula = formulas.getString(2);
        String sheetName = formulas.getString(6);
        int row = formulas.getInt(7);
        String c = formulas.getString(8);
        int col = CellReference.convertColStringToIndex(c);
        boolean usedNames = formulas.getBoolean(9);
        
        String r1c1 = POIUtils.toR1C1String(formula, row, col);
        //System.out.println(r1c1);
        if (seenFreqs.containsKey(r1c1)) {
          seenFreqs.put(r1c1, seenFreqs.get(r1c1) + 1);
        } else {
          seenFreqs.put(r1c1, 1);
          seenIds.put(r1c1, id);
          
          insert.setInt(1, id);           insert.setString(2, formula);
          insert.setString(3, file);      insert.setInt(4, sheet);
          insert.setString(5, sheetName); insert.setInt(6, row);
          insert.setString(7, c);         insert.setBoolean(8, usedNames);
          insert.executeUpdate();
        }
      }
      
      for (String r1c1 : seenIds.keySet()) {
        update.setInt(1, seenFreqs.get(r1c1));
        update.setInt(2, seenIds.get(r1c1));
        update.executeUpdate();
      }
      
    }
  }
}