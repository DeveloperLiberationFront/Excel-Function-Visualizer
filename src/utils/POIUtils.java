package utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class POIUtils {
  /**
   * Get the XSSFWorkbook for a given filename, and fails if the file doesn't exist.
   * 
   * @param filename  The name of the spreadsheet file.
   * @return          The XSSFWorkbook for the file that corresponds to the name.
   */
  public static XSSFWorkbook getWorkbook(String filename) {
    if (!filename.contains("\\") && !filename.contains("/")) 
      filename = ".\\testSheets\\" + filename;
    
    File file = new File(filename);
    return getWorkbook(file);
  }

  /**
   * Get the XSSFWorkbook for a given file, and fails if the file doesn't exist.
   * @param file  The spreadsheet file.
   * @return      The XSSFWorkbook for that file.
   */
  public static XSSFWorkbook getWorkbook(File file) {
    XSSFWorkbook wb = null;

    try {
      wb = new XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ));
    } catch (InvalidFormatException | IOException e) {
      fail("File not found");
    }
    
    return wb;
  }
  
  /**
   * Because I kept forgetting.
   * @param wb  The spreadsheet!
   * @return    The parsing workbook!
   */
  public static FormulaParsingWorkbook getParser(XSSFWorkbook wb) {
    return XSSFEvaluationWorkbook.create(wb);
  }
  
  /**
   * Because I kept forgetting. Literally the same as getParser
   * sans name and return type.
   * @param wb  The spreadsheet!
   * @return    The rendering workbook!
   */
  public static FormulaRenderingWorkbook getRender(XSSFWorkbook wb) {
    return XSSFEvaluationWorkbook.create(wb);
  }
}