package tests;

import org.junit.Test;

public class SheetTests {

  @Test
  public void test_01_albertmeyers01() {
    String filename = "./testSheets/albert_meyers__1__1-25act.xlsx";    
    TestUtils.parseFullFile(filename);    
  }
  
  /**
   * PROBLEM: java.lang.AssertionError: Parse error near char 0'
   */
  @Test
  public void test_02_andyzipper01() {
    String filename = "./testSheets/andy_zipper__112__mODEL 3 7 01 Base.xlsx";    
    TestUtils.parseFullFile(filename);    
  }
  
  /**
   * PROBLEM: Parse error near char 4 ''' in specified formula '[1]!'NGH1,PRIM ACT 1''. 
   * Expected number, string, or defined name (0:4:2:)
   */
  @Test
  public void test_03_barrytycholiz01() {
    String filename = "./testSheets/barry_tycholiz__880__EPNG Results.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * PROBLEM: 6:49:4::IF(D6<='ASSUM1'!M42,'ASSUM1'!#REF!/100,IF(D6<='ASSUM1'!M43,'ASSUM1'!N43/100,'ASSUM1'!N44/100))
   *          6:49:4::IF(D6<='ASSUM1'!M42,#REF!/100,IF(D6<='ASSUM1'!M43,'ASSUM1'!N43/100,'ASSUM1'!N44/100)) 
   */
  @Test
  public void test_04_benjaminrogers01() {
    String filename = "./testSheets/benjamin_rogers__1013__Pro Forma2.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * benjamin_rogers__1239__Simple Cycle Florida model.xlsx
   */
  @Test
  public void test_05_benjaminrogers02() {
    String filename = "./testSheets/benjamin_rogers__1239__Simple Cycle Florida model.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * Disappearing $ numbers.
   */
  @Test 
  public void test_06_benjaminrogers03() {
    String filename = "./testSheets/benjamin_rogers__909__PJMupdate1.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 2(TCPLMap):35:20::VLOOKUP(T36,[2]data!$E$1:$FM$65536,[2]data!$FI$1)/36.66/28.174
   * 2(TCPLMap):35:20::VLOOKUP(T36,[2]data!E:FM,[2]data!$FI$1)/36.66/28.174
   */
  @Test
  public void test_07_chrisdorland01() {
    String filename = "./testSheets/chris_dorland__1586__opspackage.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * java.lang.AssertionError: Second part of cell reference expected after sheet name at index 8.
   */
  @Test
  public void test_08_chrisdorland02() {
    String filename = "./testSheets/chris_dorland__1588__Reuters CQG Model.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 1(Preschedule):30:1::SUM(Deals!F9:'Deals'!F16)
   * 1(Preschedule):30:1::SUM(Deals!F9:Deals!F16)
   */
  @Test
  public void test_09_craigdean01() {
    String filename = "./testSheets/craig_dean__4356__11-9act.xlsx";
    TestUtils.parseFullFile(filename);
  }
}
