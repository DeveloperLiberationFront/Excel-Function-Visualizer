package scripts.trees;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.FormulaToken;
import core.Parser;
import utils.DBUtils;
import utils.POIUtils;

public class ViewTrees {
  public static void main(String[] args) throws SQLException, InvalidFormatException, IOException {
    Connection con = DBUtils.connectToDatabase();
    ResultSet rs = con.createStatement().executeQuery("SELECT * FROM funcs LIMIT 1;");
    
    while (rs.next()) {
      String filepath = System.getenv("ENRON_DIR") + "\\" +  rs.getString(4),
             formula = rs.getString(2);
      int sheet = rs.getInt(5);
      
      XSSFWorkbook wb = POIUtils.getWorkbook(filepath);
      FormulaParsingWorkbook parse = POIUtils.getParser(wb);
      FormulaToken tree = Parser.parseFormula(formula, sheet, parse);
      
      System.out.println(tree.toTreeString());
    }
    
  }
}