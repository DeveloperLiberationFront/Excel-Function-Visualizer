package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
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
            String coord = i + ":" + cell.getRowIndex() + ":" + cell.getColumnIndex() + ":";
            ++numOfTests;
            
            String formula = cell.getCellFormula();
            Ptg[] tokens = FormulaParser.parse(formula, parse, FormulaType.CELL, i);
            
            String result = Parser.parseFormula(tokens, render);
            compare(coord+":"+formula, coord+":"+result);                       //Add 'i' so I can see sheet number in comparison.
          }          
        }
      }
      
    }
    
    System.out.println(numOfTests + " tests successful!");
  }

  /**
   * 
   * @param formula
   * @param result
   */
  public static void compare(String formula, String result) {
    String formulaNoWhite = removeWhiteSpace(formula),
           resultNoWhite = removeWhiteSpace(result);
    assertEquals(formulaNoWhite, resultNoWhite);
  }
  
  /* TODO: What about whitespace in quotes? */
  /**
   * 
   * @param str
   * @return
   */
  public static String removeWhiteSpace(String str) {
    return str.replaceAll("[ \t\r\n]", "");
  }
}