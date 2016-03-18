package scripts.filesAndDB;

import java.io.File;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.FormulaToken;
import core.Parser;
import utils.POIUtils;

public class FindBetterFormulas {
  private static final String SSDIRECTORY = "./sheets/ENRON/";

  public static void main(String[] args) throws Exception {
    File dir = new File(SSDIRECTORY);
    Parser.dontReplace();
    
    if (!dir.isDirectory()) {
      System.err.println("SSDirectory is not a directory!");
      return;
    }
        
    for (File file : dir.listFiles()) {
      if (!file.isFile()) continue;
      
      XSSFWorkbook wb = null;

      try {
        wb = new XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ));
        FormulaParsingWorkbook parse = XSSFEvaluationWorkbook.create(wb);
        iterateOverSheets(wb, parse);
      } catch (Exception | Error e) {
        System.out.println(file);
        System.out.println(e);
        System.out.println();
      } finally {
        if (wb != null) {
          wb.close();  //If it throws an IOException here, I'll just give up.
        }
      }
      
    }
  }

  /**
   * 
   * @param wb
   * @param parse
   */
  private static void iterateOverSheets(Workbook wb, FormulaParsingWorkbook parse) {    
    for (int i = 0; i < wb.getNumberOfSheets(); ++i) {
      Sheet sheet = wb.getSheetAt(i);

      for (Row row : sheet) {
        for (Cell cell : row) {
          
        	if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {        
        	  FormulaToken tok = null;
        	  
        		try {
              String formula = cell.getCellFormula();   
              
              System.out.println(formula);
              
              //System.out.println(POIUtils.toR1C1String(formula, cell.getRowIndex(), cell.getColumnIndex()));
           
              System.out.println(">>> " + formula);
              System.out.println();
        		  tok = Parser.parseFormula(formula, parse, i);
        		} catch (Exception | Error e) {
        		  System.out.println(e + " (" + cell.getRowIndex() + "," + cell.getColumnIndex() + ")");
        		  continue;
        		}
         	}          
        }
      }
      
    }
  }


  
}