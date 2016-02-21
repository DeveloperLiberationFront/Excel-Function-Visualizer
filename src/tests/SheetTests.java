package tests;

import org.junit.Test;

public class SheetTests {

  @Test
  public void test_01_albertmeyers01() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx";    
    TestUtils.parseFullFile(filename);    
    assert(true);
  }
  
  /**
   * PROBLEM: java.lang.AssertionError: Parse error near char 0'
   */
  @Test
  public void test_02_andyzipper01() {
    //C:\Users\Justin\Documents\School\FirstYear\SpreadsheetVisualization\ENRON\SS\andy_zipper__112__mODEL 3 7 01 Base.xlsx
    String filename = "./testSheets/andy_zipper__112__mODEL 3 7 01 Base.xlsx";    
    TestUtils.parseFullFile(filename);    
    assert(true);
  }

}
