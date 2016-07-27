package scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.FormulaToken;
import core.Parser;

/**
 * End-to-end pipeline for sending spreadsheets to JSON without need for
 * a database.
 * @author Justin A. Middleton 
 * @since 27 July 2016
 */
public class SheetAnalysis {
  private static final String INPUT_DIRECTORY = "./sheets/ENRON/";
  private static final String OUTPUT_DIRECTORY = "./src/viz/json";
  
  /**
   * Ingests spreadsheets and outputs JSON files that describe the frequencies
   * with which functions are used and how functions are combined. 
   * TODO: Include example of what a JSON file looks like in this main comment.
   * 
   * @param args  No external args currently expected when running this file.
   */
  public static void main(String[] args) {  
    SheetAnalysis analysis = new SheetAnalysis();
    File sheetDirectory = new File(INPUT_DIRECTORY);    
    
    analysis.analyzeFilesIn(sheetDirectory);
  }

  /**
   * Opens a directory and analyzes every spreadsheet within.
   * Analysis here means to check every cell for formula. If the cell
   * contains a formula, then store information about the formula's
   * structure.
   * 
   * @param sheetDirectory  The directory which contains the spreadsheets
   *                            you want to analyze.
   */
  public void analyzeFilesIn(File sheetDirectory) {
    if (!sheetDirectory.isDirectory()) {
      System.err.println("SSDirectory is not a directory!");
      return;
    }
    
    Orchard trees = new Orchard();           
    ExceptionCatcher catcher = new ExceptionCatcher();
    Queue<File> directoriesToAnalyze = new ArrayDeque<File>();  
    directoriesToAnalyze.add(sheetDirectory);                   
                                                                
    while (directoriesToAnalyze.isEmpty()) {
      
      for (File file : sheetDirectory.listFiles()) {
        if (file.getName().endsWith(".ignore")) {
          continue;
        } else if (file.isDirectory()) {
          directoriesToAnalyze.add(file);
          continue;
        }
        
        XSSFWorkbook workbook;

        try {
          workbook = new XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ));
        } catch (IOException | InvalidFormatException ex) {
          catcher.addFile(file.getName(), ex);
          continue;
        }
        
        FormulaParsingWorkbook parse = XSSFEvaluationWorkbook.create(workbook);
        
        //TODO: R1C1 Correction
        
        for (int i = 0; i < workbook.getNumberOfSheets(); ++i) {
          Sheet sheet = workbook.getSheetAt(i);
          for (Row row : sheet) {
            for (Cell cell : row) {
              if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                FormulaToken formula; 
                String formulaStr = cell.getCellFormula();
                
                try {
                  formula = Parser.parseFormula(formulaStr, parse, i);
                } catch (Exception ex) {
                  catcher.addFormula(formulaStr, ex);
                  continue;
                }
                
                trees.add(formula);              
              }
            }
          }
        }
        
      } //end for (File file : sheetDirectory.listFiles())
      
    } // end while (directoriesToAnalyze.isEmpty())
    
    catcher.flushFiles();
    catcher.flushFormulae();
    trees.flush(OUTPUT_DIRECTORY);
  }
}