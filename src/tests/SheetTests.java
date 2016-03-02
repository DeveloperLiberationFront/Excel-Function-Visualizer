package tests;

import org.junit.Test;

import utils.TestUtils;

public class SheetTests {

  @Test
  public void test_01_albertmeyers01() {
    String filename = "./sheets/albert_meyers__1__1-25act.xlsx";    
    TestUtils.parseFullFile(filename);    
  }
  
  /**
   * PROBLEM: java.lang.AssertionError: Parse error near char 0'
   */
  @Test
  public void test_02_andyzipper01() {
    String filename = "./sheets/andy_zipper__112__mODEL 3 7 01 Base.xlsx";    
    TestUtils.parseFullFile(filename);    
  }
  
  /**
   * PROBLEM: Parse error near char 4 ''' in specified formula '[1]!'NGH1,PRIM ACT 1''. 
   * Expected number, string, or defined name (0:4:2:)
   */
  @Test
  public void test_03_barrytycholiz01() {
    String filename = "./sheets/barry_tycholiz__880__EPNG Results.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * PROBLEM: 6:49:4::IF(D6<='ASSUM1'!M42,'ASSUM1'!#REF!/100,IF(D6<='ASSUM1'!M43,'ASSUM1'!N43/100,'ASSUM1'!N44/100))
   *          6:49:4::IF(D6<='ASSUM1'!M42,#REF!/100,IF(D6<='ASSUM1'!M43,'ASSUM1'!N43/100,'ASSUM1'!N44/100)) 
   */
  @Test
  public void test_04_benjaminrogers01() {
    String filename = "./sheets/benjamin_rogers__1013__Pro Forma2.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * benjamin_rogers__1239__Simple Cycle Florida model.xlsx
   */
  @Test
  public void test_05_benjaminrogers02() {
    String filename = "./sheets/benjamin_rogers__1239__Simple Cycle Florida model.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * Disappearing $ numbers.
   */
  @Test 
  public void test_06_benjaminrogers03() {
    String filename = "./sheets/benjamin_rogers__909__PJMupdate1.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 2(TCPLMap):35:20::VLOOKUP(T36,[2]data!$E$1:$FM$65536,[2]data!$FI$1)/36.66/28.174
   * 2(TCPLMap):35:20::VLOOKUP(T36,[2]data!E:FM,[2]data!$FI$1)/36.66/28.174
   */
  @Test
  public void test_07_chrisdorland01() {
    String filename = "./sheets/chris_dorland__1586__opspackage.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * java.lang.AssertionError: Second part of cell reference expected after sheet name at index 8.
   */
  @Test
  public void test_08_chrisdorland02() {
    String filename = "./sheets/chris_dorland__1588__Reuters CQG Model.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 1(Preschedule):30:1::SUM(Deals!F9:'Deals'!F16)
   * 1(Preschedule):30:1::SUM(Deals!F9:Deals!F16)
   */
  @Test
  public void test_09_craigdean01() {
    String filename = "./sheets/craig_dean__4356__11-9act.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 0(#1):3:3::IF(C4>1,C4-B4,0)
   * 0(GELPOILDELIVERYSHEET#1):3:3::IF(C4>1,C4-B4,0)
   */
  @Test
  public void test_10_chrisgermany01() {
    String filename = "./sheets/chris_germany__2913__Fall 99GELP OIL BY VENDOR.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 4(Summary):26:2::+'Edison Int'#REF!
   * 4(Summary):26:2::+#REF!
   * PROBLEM: There were initially multiple quotes before the #, and it got rid of only the first.
   */
  @Test
  public void test_11_elizabethsager() {
    String filename = "./sheets/elizabeth_sager__9473__California Exposure 033001a.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 5(QueryPage):4:7::"SELECTrms_open_position.REF_DT,rms_open_position.BOOK_ID,rms_open_position.PR_CRV_CD,Sum(rms_open_position.DELTA_POSITION),Sum(rms_open_position.BENCHMARK_POSITION_QTY)FROMrms_open_positionWHERE(rms_open_position.EFF_DT='" & E5 & "')AND(rms_open_position.BOOK_IDIN("&BookList&"))GROUPBYrms_open_position.BOOK_ID,rms_open_position.REF_DT,rms_open_position.PR_CRV_CD"
   * 5(QueryPage):4:7::"SELECTrms_open_position.REF_DT,rms_open_position.BOOK_ID,rms_open_position.PR_CRV_CD,Sum(rms_open_position.DELTA_POSITION),Sum(rms_open_position.BENCHMARK_POSITION_QTY)FROMrms_open_positionWHERE(rms_open_position.EFF_DT='"&E5&"')AND(rms_open_position.BOOK_IDIN("&BookList&"))GROUPBYrms_open_position.BOOK_ID,rms_open_position.REF_DT,rms_open_position.PR_CRV_CD"
   */
  @Test
  public void test_12_frankermis() {
    String filename = "./sheets/frank_ermis__11206__HS_WESTminusPA1115.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 7(HubTracking):1:6::IF(E2=21:21,E$22:E$23,"")
   * 7(HubTracking):1:6::IF(E2=$A21:$XFD21,E$22:E$23,"")
   */
  @Test
  public void test_13_geraldnemec() {
    String filename = "./sheets/gerald_nemec__11555__TPALIST _ENAUP.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 5(DWRpurchases):61:3::++5155000+3985000+1194000
   * 5(DWRpurchases):61:3::+5155000+3985000+1194000
   */
  @Test
  public void test_14_jeffdasovich() {
    String filename = "./sheets/jeff_dasovich__14070__m010508.xlsx";
    TestUtils.parseFullFile(filename);
  }
  
  /**
   * 0(Sheet1):55:159::[1]#REF!
   * 0(Sheet1):55:159::#REF!
   */
  @Test
  public void test_15_jimschwieger() {
    String filename = "./sheets/jim_schwieger__14383__AGA_PREDICTION_1998.xlsx";
    TestUtils.parseFullFile(filename);
  }
}
