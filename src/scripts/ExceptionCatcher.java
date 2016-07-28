package scripts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExceptionCatcher {
  private Map<String, Exception> badFiles = new HashMap<String, Exception>();
  private Map<String, Exception> badFormulae = new HashMap<String, Exception>();
  
  public void addFile(String file, Exception ex) {
    badFiles.put(file, ex);
  }
  
  public void addFormula(String formula, Exception ex) {
    badFormulae.put(formula, ex);
  }
  
  public int countFiles() {
    return badFiles.size();
  }
  
  public int countFormulae() {
    return badFormulae.size();
  }
  
  /**
   * Print out every problematic file and the exception it threw.
   */
  public void flushFiles() {   
    flush("./src/scripts/badFiles.txt", badFiles);
    badFiles = new HashMap<String, Exception>();
  }
  
  /**
   * Print out every problematic formula and the exception it threw.
   */
  public void flushFormulae() {
    flush("./src/scripts/badFormulae.txt", badFormulae);
    badFormulae = new HashMap<String, Exception>();
  }

  private void flush(String output, Map<String, Exception> buffer) {
    BufferedWriter write;
    try {
      write = new BufferedWriter(new FileWriter(output, true));
    } catch (IOException ex) {
      System.err.println("Unable to flush files");
      return;
    }
    
    for (String str : buffer.keySet()) {
      Exception ex = buffer.get(str);
      try {
        write.write(str + " : ");
        write.write(ex + ", " + ex.getMessage());
      } catch (IOException ex2) {
        System.err.println("Unable to print " + str + ":" + ex.getMessage());
      }
    }
        
    try {
      write.flush();
      write.close();
    } catch (IOException ex) {
      System.err.println("Unable to close file writer: " + ex.getMessage());
    }
  }
}