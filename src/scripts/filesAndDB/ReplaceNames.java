package scripts.filesAndDB;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.FormulaToken;
import core.Parser;
import utils.DBUtils;

public class ReplaceNames {
  private static PreparedStatement update;
  
  public static void main(String[] args) throws Throwable {
    FormulaToken.dontReplace();
    Connection con = DBUtils.connectToDatabase();
    update = con.prepareStatement("UPDATE formulas SET formula = ?, usedNames = 1 WHERE id = ?;");
    Statement s1 = con.createStatement(), s2 = con.createStatement();
    ResultSet files = s1.executeQuery("SELECT DISTINCT file FROM formulas;");
    
    while (files.next()) {
      String file = files.getString(1);
      XSSFWorkbook wb = new XSSFWorkbook(OPCPackage.open("./sheets/ENRON/" + file, PackageAccess.READ));
      XSSFEvaluationWorkbook eval = XSSFEvaluationWorkbook.create(wb);

      ResultSet rs = s2.executeQuery("SELECT * FROM formulas WHERE file = \"" + file + "\";");
      while (rs.next()) {
        int id = rs.getInt(1);
        String formula = rs.getString(2);
        int sheet = rs.getInt(5);
        
        try {
          Parser.parseFormula(formula);
        } catch (FormulaParseException e) {
          fixName(id, formula, eval, sheet);
        }
        
      }
    }    
  }

  private static void fixName(int id, String formula, XSSFEvaluationWorkbook eval, int sheet) throws Throwable {
    System.out.println(id + " " + formula + " " + sheet);
    System.out.print("");
    
    try {
      FormulaToken tok = Parser.parseFormula(formula, eval, sheet);
      Parser.parseFormula(tok.toString());
      update.setString(1, tok.toString());
      update.setInt(2, id);
      update.executeUpdate();
    } catch (Exception | Error e) {
      //:(
      System.err.println("NOOOOOO");
      throw e;
    }
  }
}