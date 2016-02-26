package tests;

import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import core.Parser;
import utils.DBUtils;
import utils.POIUtils;
import utils.TestUtils;

//http://stackoverflow.com/questions/358802/junit-test-with-dynamic-number-of-tests
@RunWith(Parameterized.class)
public class BlankTests {
  private FormulaParsingWorkbook parse;
  private String formula;
  
  public BlankTests(String formula, String file) {
    this.formula = formula;
    this.parse = POIUtils.getParser(POIUtils.getWorkbook(System.getenv("ENRON_DIR") + file));
  }
  
  @Parameters(name="{0}")
  public static Collection<Object[]> getFiles() throws SQLException {
    Collection<Object[]> files = new ArrayList<Object[]>();
    
    Connection con = DBUtils.connectToDatabase();
    ResultSet rs = con.createStatement().executeQuery("SELECT * from formulas WHERE id % 18537 = 0 LIMIT 1000");
    
    while (rs.next()) {
      String function = rs.getString(2),
             file = rs.getString(4);
            
      Object[] objects = new Object[] { function, file };
      files.add(objects);
    }
    
    return files;
  }
  
  @Test
  public void test() {
    try {
      String result = Parser.parseFormula(formula, 0, Parser.BLANK).toString();
      TestUtils.compare(formula, result);
    } catch (Exception | Error e) {
      fail(e.getMessage());
    }
  }
  
}