package scripts.filesAndDB;

import java.io.File;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.Parser;

public class FindFormulas {
  private static final String SSDIRECTORY = System.getenv("ENRON_DIR");

  public static void main(String[] args) throws Exception {
    File dir = new File(SSDIRECTORY);
    
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
        		String formula = cell.getCellFormula();        		
        		System.out.println(">>> " + formula);
        		Parser.parseFormula(formula, i, parse);
        	}          
        }
      }
      
    }
  }


  
}