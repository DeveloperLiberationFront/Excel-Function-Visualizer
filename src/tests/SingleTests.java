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
    String formula = getFormulaAt(wb, sheetNum, row, col);
    
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

  //java.lang.AssertionError: Parse error near char 4 ''' in specified formula '[1]!'NGH1,PRIM ACT 1''. Expected number, string, or defined name
  @Test
  public void test_07_singlequote() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx",
           formula = "[1]!'NGH1,PRIM ACT 1'";
    int    sheetNum = 0;
    
    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
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

  //IF(D6<='ASSUM1'!M42,'ASSUM1'!#REF!/100,IF(D6<='ASSUM1'!M43,'ASSUM1'!N43/100,'ASSUM1'!N44/100))
  @Test
  public void test_08_sheetBadRef() {
    String filename = "./testSheets/benjamin_rogers__1013__Pro Forma2.xlsx";
    int    sheetNum = 6, row = 48, col = 3;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  @Test
  public void test_09_ampersand() {
    String filename = "./testSheets/benjamin_rogers__1239__Simple Cycle Florida model.xlsx";
    int    sheetNum = 11, row = 31, col = 3;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  //1(RawData):0:21::MATCH(Strips!B1,A:A,0)-2
  @Test
  public void test_10_dollarsign() {
    String filename = "./testSheets/sum.xlsx",
           formula = "SUM(A:A)";
    int    sheetNum = 1;
    
    singleSuccessTest(filename, formula, sheetNum);
  }
  
  /**
   * 2(TCPLMap):35:20::VLOOKUP(T36,[2]data!$E$1:$FM$65536,[2]data!$FI$1)/36.66/28.174
   * 2(TCPLMap):35:20::VLOOKUP(T36,[2]data!E:FM,[2]data!$FI$1)/36.66/28.174
   */
  @Test
  public void test_11_dollararea() {
    String filename = "./testSheets/chris_dorland__1586__opspackage.xlsx";
    int sheetNum = 2, row = 35, col = 20;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * java.lang.AssertionError: Second part of cell reference expected after sheet name at index 8.
   */
  @Test
  public void test_12_index8() {
    String filename = "./testSheets/chris_dorland__1588__Reuters CQG Model.xlsx";
    int sheetNum = 0, row = 3, col = 4;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * 1(Preschedule):30:1::SUM(Deals!F9:'Deals'!F16)
   * 1(Preschedule):30:1::SUM(Deals!F9:Deals!F16)
   */
  @Test
  public void test_13_disappearingquotes() {
    String filename = "./testSheets/craig_dean__4356__11-9act.xlsx";
    int sheetNum = 1, row = 30, col = 1;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  private void singleSuccessTest(String filename, int sheetNum, int row, int col) {
    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    String formula = getFormulaAt(wb, sheetNum, row, col);
    singleSuccessTest(filename, formula, sheetNum);
  }
  
  private void singleSuccessTest(String filename, String formula, int sheetNum) {
    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse);
    TestUtils.compare(formula, result);
  }
  
  private String getFormulaAt(XSSFWorkbook wb, int sheetNum, int row, int col) {
    return wb.getSheetAt(sheetNum)
        .getRow(row)
        .getCell(col)
        .toString();
  }
  
}
