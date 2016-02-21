package tests;

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
    
    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse);
    TestUtils.compare(formula, result);
  }
  
  @Test
  public void test_02_sum() {
    String filename = "./testSheets/sum.xlsx",
           formula = "IF(A1>A2, SUM(A3:A4), SUM(A5, A6))";
    int    sheetNum = 0;
    
    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse);
    TestUtils.compare(formula, result);
  }
  
  //TODO: MissingArgPtg present, which included space in my parser's result.
  @Test
  public void test_03_blanksum() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx",
           formula = "IF(-SUM(B18:B20,B35:B37)>134,\"err\",-SUM(B18:B20,))";
    int    sheetNum = 0;

    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse);
    TestUtils.compare(formula, result);
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

    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse);
    TestUtils.compare(formula, result);
  }
  
  /**
   * PROBLEM: Third-party functions throw an error.
   */
  @Test
  public void test_05_thirdparty() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx",
           formula = "IndGencost(D104,OFFSET(HeatRateStart,1,MATCH($A104,HeatRateNames,0)-1,250,1),$A105)";
    int    sheetNum = 0;

    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse);
    TestUtils.compare(formula, result);
  }
}
