package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.Parser;

public class TestUtils {
  /**
   * 
   * @param filename
   * @return
   */
  public static XSSFWorkbook getWorkbook(String filename) {
    File file = new File(filename);
    XSSFWorkbook wb = null;

    try {
      wb = new XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ));
    } catch (InvalidFormatException | IOException e) {
      fail("File not found");
    }
    
    return wb;
  }
  
  /**
   * 
   * @param wb
   * @param parse
   * @param render
   */
  public static void iterateOverFormulas(XSSFWorkbook wb, FormulaParsingWorkbook parse, FormulaRenderingWorkbook render) {
    int numOfTests = 0;
    
    for (int i = 0; i < wb.getNumberOfSheets(); ++i) {
      Sheet sheet = wb.getSheetAt(i);

      for (Row row : sheet) {
        for (Cell cell : row) {
          
          if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            String coord = cellToString(cell);
            ++numOfTests;
            
            String formula = getCell(cell);
            Ptg[] tokens = FormulaParser.parse(formula, parse, FormulaType.CELL, i);
            
            String result = Parser.parseFormula(tokens, render);
            compare(coord+":"+formula, coord+":"+result);                       //Add 'i' so I can see sheet number in comparison.
          }          
        }
      }
      
    }
    
    System.out.println(numOfTests + " tests successful!");
  }

  private static String getCell(Cell cell) {
    String formula = "";
    
    try {
      formula = cell.getCellFormula();
    } catch (FormulaParseException e) {
      System.err.println(cellToString(cell));
      fail(e.getMessage());
    }
    
    return formula;
  }

  /**
   * 
   * @param formula
   * @param result
   */
  public static void compare(String formula, String result) {
    String formulaNoWhite = format(formula),
           resultNoWhite = format(result);
    assertEquals(formulaNoWhite, resultNoWhite);
  }
  
  /* TODO: What about whitespace in quotes? */
  /**
   * 1. Remove white space
   * 2. Remove sheets for errors.
   * @param str
   * @return
   */
  public static String format(String str) {
    return str
            .replaceAll("[ \t\r\n]", "")
            .replaceAll("\\w+!#", "#");   //Added in test_04
  }
  
  /**
   * 
   * @param cell
   * @return
   */
  private static String cellToString(Cell cell) {
    return cell.getSheet().getSheetName() + ":" + cell.getRowIndex() + ":" + cell.getColumnIndex() + ":";
  }
}