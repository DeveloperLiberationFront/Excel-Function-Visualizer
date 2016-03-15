package scripts.filesAndDB;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.poi.hssf.util.CellReference;
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

import utils.DBUtils;

public class StoreBetterFunctionsInDB {
  private static String insert = "INSERT INTO formulas "
      + "(formula, src, file, sheet, sheetName, row, col, usedNames) "
      + "VALUES (?, 'ENRON', ?, ?, ?, ?, ?, 0);";
  private static PreparedStatement ps = null;

  public static void main(String[] args) throws Throwable {
    Connection con = DBUtils.connectToDatabase();
    ps = con.prepareStatement(insert);

    String ENRON = "./sheets/ENRON/";
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
        //throw e;

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
                store(formula, file, i, sheet.getSheetName(), cell.getRowIndex(), CellReference.convertNumToColString(cell.getColumnIndex()));
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

    //ps.executeBatch();
  }

  static int total = 0, batchsize = 1000, tooLarge = 0, error = 0;
  private static void store(String formula, String file, int sheet, String sheetName, int rowIndex, String column) 
      throws SQLException {

    if (formula.length() > 1200 || file.length() > 150) {
      System.err.println(formula);
      System.err.println(file + " " + sheet + " " + rowIndex + " " + column);
      System.err.println();
      ++tooLarge;
      return;
    }

    try {
//      ps.setString(1, formula);
//      ps.setString(2, file);
//      ps.setInt(3, sheet);
//      ps.setString(4, sheetName);
//      ps.setInt(5, rowIndex);
//      ps.setString(6, column);
//      ps.addBatch();
      ++total;
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println("\t" + formula);
      System.err.println();
    }

//    if (total % batchsize == 0) {
//      ps.executeBatch();
//    }

  }

  private static boolean isUseful(Ptg[] tokens) {
    if (tokens.length == 1) {
      Ptg token = tokens[0];
      if (token instanceof ErrPtg || token instanceof ScalarConstantPtg || token instanceof OperandPtg) {
        return false;
      }
    }

    return true;
  }
}