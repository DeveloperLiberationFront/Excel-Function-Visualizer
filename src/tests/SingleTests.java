package tests;

import static org.junit.Assert.fail;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import core.Parser;

public class SingleTests {

  @Test
  public void test_01() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx",
           formula = "SUM(IF((DelPoint= \"4C\")*(DType = \"pre\")*(OFFSET(DelPoint,0,B3+2)<0),OFFSET(DelPoint,0,B3+2),0))";
    int    sheetNum = 0;
    
    singleSuccessTest(filename, formula, sheetNum);
  }
  
  @Test
  public void test_02_sum() {
    String filename = "./testSheets/sum.xlsx",
           formula = "IF(A1>A2, SUM(A3:A4), SUM(A5, A6))";
    int    sheetNum = 0;
    
    singleSuccessTest(filename, formula, sheetNum);
  }
  
  //TODO: MissingArgPtg present, which included space in my parser's result.
  @Test
  public void test_03_blanksum() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx",
           formula = "IF(-SUM(B18:B20,B35:B37)>134,\"err\",-SUM(B18:B20,))";
    int    sheetNum = 0;

    singleSuccessTest(filename, formula, sheetNum);
  }
  
  /**
   * PROBLEM: Deals!#REF! becomes only #REF! when parsed.
   * SOLUTION: In compare function, remove sheet name in formula string for those errors.
   */
  @Test
  public void test_04_referror() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx",
           formula = "IF(SUM(Deals!#REF!)=0,0,SUM(Deals!#REF!*Deals!#REF!)/SUM(Deals!#REF!))";
    int    sheetNum = 0;

    singleSuccessTest(filename, formula, sheetNum);
  }
  
  /**
   * PROBLEM: Third-party functions throw an exception (FormulaParseException)
   * TODO: Either change the expected thrown Exception or define own exception.
   */
  @Test
  public void test_05_thirdparty() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx",
           formula = "IndGencost(D104,OFFSET(HeatRateStart,1,MATCH($A104,HeatRateNames,0)-1,250,1),$A105)";
    int    sheetNum = 0;

    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    
    try {
      Parser.parseFormula(formula, sheetNum, parse);
      fail("No FormulaParseException thrown");
    } catch (FormulaParseException e) {
      assert(true);
    } catch (Exception | Error e) {
      fail("Wrong exception/error caught: " + e.getClass());
    }
      
  }
  
  /**
   * PROBLEM: java.lang.AssertionError: Parse error near char 0'
   *          Even though the formula is in the cell, trying to
   *          access it yields an empty string.
   * SOLUTION:Throw and handle an UnsupportedOperationException.
   */
  @Test
  public void test_06_parseerrornear0() {
    //0%
    String filename = "./testSheets/andy_zipper__112__mODEL 3 7 01 Base.xlsx";
    int    sheetNum = 2, row = 42, col = 4;
    
    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    String formula = wb.getSheetAt(sheetNum)
        .getRow(row)
        .getCell(col)
        .toString();
    
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    
    try {
      Parser.parseFormula(formula, sheetNum, parse);
      fail("No UnsupportedOperationException caught.");
    } catch (UnsupportedOperationException e) {
      //Success!
    } catch (Exception | Error e) {
      fail("Wrong exception/error caught.");
    }
  }
  
  private void singleSuccessTest(String filename, String formula, int sheetNum) {
    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse);
    TestUtils.compare(formula, result);
  }
}
