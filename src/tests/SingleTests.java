package tests;

import static org.junit.Assert.fail;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import core.FormulaToken;
import core.Parser;
import utils.POIUtils;
import utils.TestUtils;

public class SingleTests {
  @Before
  public void setUp() {
    FormulaToken.dontReplace();
  }
  

  @Test
  public void test_01() {
    String filename = "./sheets/ENRON/albert_meyers__1__1-25act.xlsx",
           formula = "SUM(IF((DelPoint= \"4C\")*(DType = \"pre\")*(OFFSET(DelPoint,0,B3+2)<0),OFFSET(DelPoint,0,B3+2),0))";
    int    sheetNum = 0;
    
    singleSuccessTest(filename, formula, sheetNum);
  }
  
  @Test
  public void test_02_sum() {
    String filename = "./src/utils/sum.xlsx",
           formula = "IF(A1>A2, SUM(A3:A4), SUM(A5, A6))";
    int    sheetNum = 0;
    
    singleSuccessTest(filename, formula, sheetNum);
  }
  
  //TODO: MissingArgPtg present, which included space in my parser's result.
  @Test
  public void test_03_blanksum() {
    String filename = "./sheets/ENRON/albert_meyers__1__1-25act.xlsx",
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
    String filename = "./sheets/ENRON/albert_meyers__1__1-25act.xlsx",
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
    String filename = "./sheets/ENRON/albert_meyers__1__1-25act.xlsx",
           formula = "IndGencost(D104,OFFSET(HeatRateStart,1,MATCH($A104,HeatRateNames,0)-1,250,1),$A105)";
    int    sheetNum = 0;

    XSSFWorkbook wb = POIUtils.getWorkbook(filename);
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
    String filename = "./sheets/ENRON/andy_zipper__112__mODEL 3 7 01 Base.xlsx";
    int    sheetNum = 2, row = 42, col = 4;
    
    XSSFWorkbook wb = POIUtils.getWorkbook(filename);
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
    String filename = "./sheets/ENRON/albert_meyers__1__1-25act.xlsx",
           formula = "[1]!'NGH1,PRIM ACT 1'";
    int    sheetNum = 0;
    
    XSSFWorkbook wb = POIUtils.getWorkbook(filename);
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
    String filename = "./sheets/ENRON/benjamin_rogers__1013__Pro Forma2.xlsx";
    int    sheetNum = 6, row = 48, col = 3;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  @Test
  public void test_09_ampersand() {
    String filename = "./sheets/ENRON/benjamin_rogers__1239__Simple Cycle Florida model.xlsx";
    int    sheetNum = 11, row = 31, col = 3;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  //1(RawData):0:21::MATCH(Strips!B1,A:A,0)-2
  @Test
  public void test_10_dollarsign() {
    String filename = "./src/utils/sum.xlsx",
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
    String filename = "./sheets/ENRON/chris_dorland__1586__opspackage.xlsx";
    int sheetNum = 2, row = 35, col = 20;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * java.lang.AssertionError: Second part of cell reference expected after sheet name at index 8.
   */
  @Test
  public void test_12_index8() {
    String filename = "./sheets/ENRON/chris_dorland__1588__Reuters CQG Model.xlsx";
    int sheetNum = 0, row = 3, col = 4;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * 1(Preschedule):30:1::SUM(Deals!F9:'Deals'!F16)
   * 1(Preschedule):30:1::SUM(Deals!F9:Deals!F16)
   */
  @Test
  public void test_13_disappearingquotes() {
    String filename = "./sheets/ENRON/craig_dean__4356__11-9act.xlsx";
    int sheetNum = 1, row = 30, col = 1;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * 4(Summary):26:2::+'Edison Int'#REF!
   * 4(Summary):26:2::+#REF!
   */
  @Test
  public void test_14_quotepound() {
    String filename = "./sheets/ENRON/elizabeth_sager__9473__California Exposure 033001a.xlsx";
    int sheetNum = 4, row = 26, col = 2;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * 0(Sheet1):55:159::[1]#REF!
   * 0(Sheet1):55:159::#REF!
   */
  @Test
  public void test_15_bracketerror() {
    String filename = "./sheets/ENRON/jim_schwieger__14383__AGA_PREDICTION_1998.xlsx";
    int sheetNum = 0, row = 55, col = 159;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * 3(Returns):7:2::XIRR($Y$7:INDEX($D3:AS$7,5,MATCH(Assumptions!$C$18,Returns!$D3:AS$3)),$Y$3:INDEX($D3:AS$3,1,MATCH(Assumptions!$C$18,Returns!$D3:AS$3)))
   * 3(Returns):7:2::$Y$7:INDEX($D3:AS$7,5,MATCH(Assumptions!$C$18,Returns!$D3:AS$3))(,$Y$3:INDEX($D3:AS$3,1,MATCH(Assumptions!$C$18,Returns!$D3:AS$3)))
   * SOLUTION: Skip MemFuncPtg
   */
  @Test
  public void test_16_outermostmissing() {
    String filename = "./sheets/ENRON/benjamin_rogers__1037__CFTejon8a revised 21 - 750 MW.xlsx";
    int sheetNum = 3, row = 7, col = 2;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * 9(BS):32:3::ProjectAssumtions!$C$8-SUM(BookIncomeStatement!$D$66:BookIncomeStatement!D66)
   * 9(BS):32:3::-SUM(BookIncomeStatement!$D$66:BookIncomeStatement!D66)
   * Skip MemAreaPtg
   */
  @Test
  public void test_17_missingtopleveloperand() {
    String filename = "./sheets/ENRON/benjamin_rogers__1172__Wheatland_New - W 501 D5A.xlsx";
    int sheetNum = 9, row = 32, col = 3;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * 1(CF):20:3::NPV($B$21,$D$20:D20)+MAX(PV($B$21,D2,,-D20/($B$21-$C$21)),SUM(Costs!$D$52:Costs!D52)-SUM(Costs!$D$55:D55))
   * 1(CF):20:3::PV($B$21,D2,,-D20/($B$21-$C$21))+MAX(,SUM(Costs!$D$52:Costs!D52)-SUM(Costs!$D$55:D55))
   * SOLUTION: Solved with 16 and 17
   */
  @Test
  public void test_18_multipletoplevelmissing() {
    String filename = "./sheets/ENRON/danny_mccarty__4661__XTRANSCO - Economics.xlsx";
    int sheetNum = 1, row = 20, col = 3;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * 1(Relation):3:2::IF(C3=1,1,LINEST(OFFSET(C$5,ABS($A$2-$A$1)+1,0):OFFSET(C$5,$A$1,0),OFFSET($B$5,ABS($A$2-$A$1)+1,0):OFFSET($B5,$A$1,0)))
   * 1(Relation):3:2::IF(,OFFSET(C$5,ABS($A$2-$A$1)+1,0):OFFSET(C$5,$A$1,0),LINEST(,OFFSET($B$5,ABS($A$2-$A$1)+1,0):OFFSET($B5,$A$1,0)))
   * SOLUTION: Solved with 16 and 17
   */
  @Test
  public void test_19_missinginternal() {
    String filename = "./sheets/ENRON/john_griffith__15855__Vol Move.xlsx";
    int sheetNum = 1, row = 3, col = 2;
    
    singleSuccessTest(filename, sheetNum, row, col);
  }
  
  /**
   * IF(AND(I676,I722>0),IF(I676-I719<=0,0,IF(I676<I722,I676,I722)),0)
   */
  @Test
  public void test_20_graphmistake1() {
    String formula = "IF(AND(I676,I722>0),IF(I676-I719<=0,0,IF(I676<I722,I676,I722)),0)";
    singleSuccessTest(formula);
  }
  
  private void singleSuccessTest(String filename, int sheetNum, int row, int col) {
    XSSFWorkbook wb = POIUtils.getWorkbook(filename);
    String formula = getFormulaAt(wb, sheetNum, row, col);
    
    /*Used to call function below, but that would create two workbooks.*/
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse).toString();
    TestUtils.compare(formula, result);
  }
  
  private void singleSuccessTest(String filename, String formula, int sheetNum) {
    XSSFWorkbook wb = POIUtils.getWorkbook(filename);
    XSSFEvaluationWorkbook parse = XSSFEvaluationWorkbook.create(wb);
    String result = Parser.parseFormula(formula, sheetNum, parse).toString();
    TestUtils.compare(formula, result);
  }
  
  private void singleSuccessTest(String formula) {
    String result = Parser.parseFormula(formula).toString();
    TestUtils.compare(formula, result);
  }
  
  private String getFormulaAt(XSSFWorkbook wb, int sheetNum, int row, int col) {
    return wb.getSheetAt(sheetNum)
        .getRow(row)
        .getCell(col)
        .toString();
  }
  
}
