package scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StoreFunctionsInDB {
  private static String insert = "INSERT INTO funcs "
      + "(function, src, file, sheet, row, col) "
      + "VALUES (?, 'ENRON', ?, ?, ?, ?);";
  private static PreparedStatement ps = null;

  public static void main(String[] args) throws Throwable {
    Connection con = connectToDatabase();
    ps = con.prepareStatement(insert);

    String ENRON = System.getenv("ENRON_DIR");
    for (File file : new File(ENRON).listFiles()) {
      if (file.isDirectory()) continue;

      String name = file.toString().replaceAll(".*\\\\", "");
      //System.out.println(name);
      XSSFWorkbook wb = null;

      try {

        wb = new XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ));
        FormulaParsingWorkbook parse = XSSFEvaluationWorkbook.create(wb);
        iterateOverSheets(name, wb, parse);

      } catch (Exception | Error e) {

        System.out.println(file);
        System.out.println(e);
        System.out.println();
        throw e;

      } finally {
        if (wb != null) {
          wb.close();  //If it throws an IOException here, I'll just give up.
        }
      }

    }

    System.out.println(total + " (" + tooLarge + ") [" + error + "] !" + single +"! #" + cantbeparsed + "#");
  }

  /**
   * 
   * @param name 
   * @param wb
   * @param parse
   * @throws SQLException 
   */
  static int single = 0, cantbeparsed = 0;
  private static void iterateOverSheets(String file, Workbook wb, FormulaParsingWorkbook parse) throws SQLException {    
    for (int i = 0; i < wb.getNumberOfSheets(); ++i) {
      Sheet sheet = wb.getSheetAt(i);

      for (Row row : sheet) {
        for (Cell cell : row) {

          if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            String formula = "";
            Ptg[] tokens = null;

            try {
              formula = cell.getCellFormula();           
              tokens = FormulaParser.parse(formula, parse, FormulaType.CELL, i);
            } catch (Exception | Error e) {
              ++cantbeparsed;
              //System.out.println(e.getMessage());
              continue;
            }

            if (isUseful(tokens)) {

              try {
                store(formula, file, i, cell.getRowIndex(), cell.getColumnIndex());
              } catch (SQLException e) {
                System.err.println(file + " " + sheet + " " + cell.getRowIndex() + " " + cell.getColumnIndex());
                System.err.println();
                continue;
              }          

            } else {
              ++single;
            }


          }          
        }
      }      
    }

    ps.executeBatch();
  }

  static int total = 0, batchsize = 1000, tooLarge = 0, error = 0;
  private static void store(String formula, String file, int sheet, int rowIndex, int columnIndex) throws SQLException {

    if (formula.length() > 900 || file.length() > 150) {
      System.err.println(formula);
      System.err.println(file + " " + sheet + " " + rowIndex + " " + columnIndex);
      System.err.println();
      ++tooLarge;
      return;
    }

    try {
      ps.setString(1, formula);
      ps.setString(2, file);
      ps.setInt(3, sheet);
      ps.setInt(4, rowIndex);
      ps.setInt(5, columnIndex);
      ps.addBatch();
      ++total;
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println("\t" + formula);
      System.err.println();
    }

    if (total % batchsize == 0) {
      ps.executeBatch();
    }

  }

  private static boolean isUseful(Ptg[] tokens) {
    if (tokens.length == 1) {
      Ptg token = tokens[0];
      if (token instanceof ErrPtg || token instanceof ScalarConstantPtg || token instanceof OperandPtg) {//ErrPtg
        //if (tokens[0].toFormulaString().equals("NOW") || tokens[0].toFormulaString().equals("TODAY")
        //    || tokens[0].toFormulaString().equals("NA") || tokens[0].toFormulaString().equals("ROW")
        //    || tokens[0].toFormulaString().equals("RAND")) {
        return false;
        //}
      }
    }

    return true;
  }

  public static Connection connectToDatabase() throws SQLException {
    Map<String, String> env = System.getenv();
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.err.println("connectToDatabase: No database driver found.");
      return null;
    }

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/spreadsheet_funcs", 
        "root", System.getenv("MYSQL_PASSWORD"));
    return con;
  }
}