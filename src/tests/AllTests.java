package tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import utils.TestUtils;

//http://stackoverflow.com/questions/358802/junit-test-with-dynamic-number-of-tests
@RunWith(Parameterized.class)
public class AllTests {
  private File file;
  
  public AllTests(String filename, File file) {
    this.file = file;
  }
  
  @Parameters(name="{0}")
  public static Collection<Object[]> getFiles() {
    Collection<Object[]> files = new ArrayList<Object[]>();
    
    String enronDir = System.getenv("ENRON_DIR");
    for (File file : new File(enronDir).listFiles()) {
      if (!file.isFile()) continue;
      
      String filename = file.toString().replaceAll(".*\\\\", "");
      
      Object[] objects = new Object[] { filename, file };
      files.add(objects);
    }
    
    return files;
  }
  
  @Test
  public void test() {
    TestUtils.parseFullFile(file);
  }
  
}