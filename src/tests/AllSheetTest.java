package tests;

import java.io.File;

import org.junit.Test;

public class AllSheetTest {
  public static final String ALL_SHEET_DIR = System.getenv("ENRON_DIR");
      
  @Test
  public void test_01_ENRON() {
    File dir = new File(ALL_SHEET_DIR);

    for (File file : dir.listFiles()) {
      if (!file.isFile()) continue;
      
      System.out.println(file.toString());
      TestUtils.parseFullFile(file);
    }
    
    assert(true);
  }

}
