package tests;

import static org.junit.Assert.*;

import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;

import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class SheetTests {

  @Test
  public void test_01_albertmeyers01() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx";    
    XSSFWorkbook wb = TestUtils.getWorkbook(filename);
    assertNotNull(wb);
    
    FormulaParsingWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    FormulaRenderingWorkbook render = (FormulaRenderingWorkbook) parse;    
    TestUtils.iterateOverFormulas(wb, parse, render);
    
    assert(true);
  }

}
