package core;

import com.google.gson.annotations.Expose;

public class Example {
  private int id;
  
  @Expose
  private String formula;
  
  @Expose
  private String file;
  
  @Expose
  private String location;
  
  public Example(int id, String formula, String file, String location) {
    this.id = id;
    this.formula = formula;
    this.file = file;
    this.location = location;
  }
  
  public int getID() {
    return this.id;
  }
  
  public String getFormula() {
    return this.formula;
  }
  
  public int getFormulaLength() {
    return this.formula.length();
  }
  
  public String getFile() {
    return this.file;
  }
  
  public String getLocation() {
    return this.location;
  }
  
  public String toString() {


    
    return id + ";" + formula + ";" + file + ";" + location;
  }
}