package scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.ptg.ErrPtg;
import org.apache.poi.ss.formula.ptg.OperandPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.ScalarConstantPtg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.Example;
import core.Token;
import core.FunctionToken;
import core.Parser;

/**
 * End-to-end pipeline for sending spreadsheets to JSON without need for
 * a database.
 * @author Justin A. Middleton 
 * @since 27 July 2016
 */
public class SheetAnalysis {
  private static final String INPUT_DIRECTORY = "./sheets/ENRON/";
  private static final String OUTPUT_DIRECTORY = "./src/viz/json/";
  
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
                            
    int exampleId = 0;
    while (!directoriesToAnalyze.isEmpty()) {
      File directory = directoriesToAnalyze.remove();
      for (File file : directory.listFiles()) {
        System.out.print(file + ": ");
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
        } catch (OutOfMemoryError ex) {
          System.err.println(ex);
          continue;
        }
        
        FormulaParsingWorkbook parse = XSSFEvaluationWorkbook.create(workbook);
        for (int i = 0; i < workbook.getNumberOfSheets(); ++i) {
          System.out.print((i + 1) + " ");
          Sheet sheet = workbook.getSheetAt(i);
          Set<String> seenRelativeFormulae = new HashSet<String>();
          
          for (Row row : sheet) {
            for (Cell cell : row) {
              if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                Token formula; 
                String r1c1;
                String formulaStr;
                CellReference cellRef;
                
                try {
                  formulaStr = cell.getCellFormula();
                } catch (FormulaParseException ex) {
                  catcher.addFormula(file + " " + cell.getRowIndex() + "," 
                      + cell.getColumnIndex(), ex);
                  continue;
                }
                
                try {
                  cellRef = new CellReference(cell);
                  formula = Parser.parseFormula(formulaStr, parse, i);
                  r1c1 = formula.toR1C1String(cellRef);                  
                } catch (Exception ex) {
                  //System.err.println(formulaStr + " : " + ex.getMessage());
                  catcher.addFormula(formulaStr, ex);
                  continue;
                }
                
                if (seenRelativeFormulae.contains(r1c1)) {
                  continue;
                } else {
                  seenRelativeFormulae.add(r1c1);
                }
                
                if (isUseful(formula)) {
                  String cellLocation = "'" + sheet.getSheetName() + "'!"  
                      + CellReference.convertNumToColString(cell.getColumnIndex()) 
                      + (cell.getRowIndex());
                  Example example = new Example(++exampleId, formula.toOrigString(), 
                      file.getName(), cellLocation);
                  trees.add(formula, example);     
                }
              }
            }
          }
        }
        
        if (catcher.countFiles() > 10) {
          catcher.flushFiles();
        }
        
        if (catcher.countFormulae() > 1000) {
          catcher.flushFormulae();
        }
        
        System.out.println();
        
        try {
          workbook.close();
        } catch (IOException ex) {
          System.err.println("Cannot close workbook.");
        }
      } //end for (File file : sheetDirectory.listFiles())
      
    } // end while (directoriesToAnalyze.isEmpty())
    
    catcher.flushFiles();
    catcher.flushFormulae();
    trees.flush(OUTPUT_DIRECTORY);
  }
  
  /**
   * Lifted from StoreBetterFunctionsInDB
   * @param tokens
   * @return
   */
  private static boolean isUseful(Token formula) {
    if (!(formula instanceof FunctionToken)) {
      return false;
    }

    return true;
  }
}