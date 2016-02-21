package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.Parser;

public class TestUtils {
  /**
   * Get the XSSFWorkbook for a given filename, and fails if the file doesn't exist.
   * 
   * @param filename  The name of the spreadsheet file.
   * @return          The XSSFWorkbook for the file that corresponds to the name.
   */
  public static XSSFWorkbook getWorkbook(String filename) {
    File file = new File(filename);
    return getWorkbook(file);
  }

  /**
   * Get the XSSFWorkbook for a given file, and fails if the file doesn't exist.
   * @param file  The spreadsheet file.
   * @return      The XSSFWorkbook for that file.
   */
  public static XSSFWorkbook getWorkbook(File file) {
    XSSFWorkbook wb = null;

    try {
      wb = new XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ));
    } catch (InvalidFormatException | IOException e) {
      fail("File not found");
    }
    
    return wb;
  }
  
  /**
   * Given a filename, test all formulas in that file.
   * @param filename  The filename (and not the File object itself)
   */
  public static void parseFullFile(String filename) {
    File file = new File(filename);
    parseFullFile(file);
  }

  /**
   * Given a spreadsheet file, test all formulas in that file.
   * @param file  The spreadsheet.
   */
  public static void parseFullFile(File file) {
    XSSFWorkbook wb = getWorkbook(file);
    assertNotNull(wb);  
    iterateOverFormulas(wb);
  }
  
  
  /**
   * Iterate and test all the formulas in a given workbook.
   * 
   * @param wb      The workbook to iterate over.
   */
  public static void iterateOverFormulas(XSSFWorkbook wb) {
    FormulaParsingWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    int numOfTests = 0, numOfParse = 0, numOfEmpty = 0;
    
    for (int i = 0; i < wb.getNumberOfSheets(); ++i) {
      Sheet sheet = wb.getSheetAt(i);

      for (Row row : sheet) {
        for (Cell cell : row) {          
          if (cell.getCellType() != Cell.CELL_TYPE_FORMULA) 
            continue;
          
          String coord = cellToString(cell, i);       
          //System.out.println(coord);
          try {
            String formula = cell.getCellFormula();
            String result = Parser.parseFormula(formula, i, parse);
            ++numOfTests;
            compare(coord+":"+formula, coord+":"+result);             
          } catch (FormulaParseException e) {
            ++numOfParse;
            handleParseError(e, coord);
          } catch (UnsupportedOperationException e) {
            //I'm assuming the only UOE I'll deal with is the one I throw...
            ++numOfEmpty;
          }
     
        }
      }
      
    }
    
    System.out.println(numOfTests + " tests successful!");
    System.out.println(numOfParse + " formulas with errors!");
    System.out.println(numOfEmpty + " formulas empty!");
    System.out.println();
  }

  /**
   * Compare the expected result with actual result, all whitespace removed.
   * TODO:  Make sure white-space in quotes remains.
   * 
   * @param formula The entered formula.
   * @param result  The return formula after parsing and piecing back together.
   */
  public static void compare(String formula, String result) {
    String formulaNoWhite = format(formula),
           resultNoWhite = format(result);
    assertEquals(formulaNoWhite, resultNoWhite);
  } 
  
  /**
   * If the test throws a FormulaParseException, figure out whether it's an expected error
   * from a third-party/user-defined function (then pass) or something else (then fail).
   * 
   * @param e     The exception to analyze.
   * @param coord The coordinates of the problem cell (as returned by cellToString()).
   *              Purely for print-out purposes.
   */
  private static void handleParseError(FormulaParseException e, String coord) {
    String thirdParty = isThirdPartyFunc(e.getMessage());              
    if (thirdParty.equals("")) {
      System.err.println(e.getMessage() + " (" + coord + ")");
      fail(e.getMessage());
    } else {
      //System.err.println("Third party: " + thirdParty + "(" + coord + ")");
    }
  }
  
  /**
   * Checks to see if a FormulaParseException is because of a third-party/user-defined
   * function, and returns the function name if it is.
   * 
   * @param message The exception message, which should be in the form of 
   *                  "Name 'foo' is completely unknown in the current workbook'
   *                if it's a third-party/user-defined function error.
   * @return        Either name of the problem function or blank string if it's another exception.
   */
  private static String isThirdPartyFunc(String message) {
    String func = "";
    boolean isThirdParty = message.trim()
                          .matches("Name '[^']+' is completely unknown in the current workbook");
    
    if (isThirdParty) {
      func = message.replaceFirst("[^']+'", "").replaceFirst("'[^']+", "");
    }
    
    return func;
  }
  
  /* TODO: What about whitespace in quotes? */
  /**
   * 1. Remove white space
   * 2. Remove sheets for errors.
   * @param str String to format.
   * @return    String with all above steps applied to it.
   */
  public static String format(String str) {
    return str
            .replaceAll("[ \t\r\n]", "")
            .replaceAll("\\w+!#", "#");   //Added in test_04
  }
  
  /**
   * Gets the coordinates of a certain cell in the spread-sheet. The functions getRowIndex
   * and -Column- return 0-indexed coordinates, but the spreadsheet is 1-indexed, so I added 
   * one to each.
   * 
   * HOWEVER, sheetIndex remains 0-index because that's how the FormulaParser.parse function
   * takes it. Annoying, I know.
   * 
   * @param cell        The cell to locate.
   * @param sheetIndex  The current sheet number. Cells record sheet names, not numbers.
   * @return            String in form of "<sheet_num>:<row_num>:<col_num>:"
   */
  private static String cellToString(Cell cell, int sheetIndex) {
    return sheetIndex + ":" + (cell.getRowIndex()+1) + ":" + (cell.getColumnIndex()+1) + ":";
  } 
}