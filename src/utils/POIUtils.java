package utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.FormulaToken;
import core.Parser;

public class POIUtils {  
  /**
   * Get the XSSFWorkbook for a given filename, and fails if the file doesn't exist.
   * 
   * @param filename  The name of the spreadsheet file.
   * @return          The XSSFWorkbook for the file that corresponds to the name.
   */
  public static XSSFWorkbook getWorkbook(String filename) {
    if (!filename.contains("\\") && !filename.contains("/")) 
      filename = "./sheets/" + filename;
    
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
  
  /**
   * Replaces a name with what it actually stands for in the workbook.
   * Returns String so the parser is forced to parse it again, because the presence of names
   *  can alter how the tokens are parsed. For example, ranges might be broken up into
   *  two reference tokens and a colon token, rather than a single range token.
   * @param token   The name to resolve.
   * @param render  The workbook it came from.
   * @param sheet   The sheet the name was in.
   * @return
   */
  public static Ptg[] resolveName(NamePtg token, FormulaRenderingWorkbook render, int sheet) {
    String nameStr = token.toFormulaString(render).trim();
    EvaluationName eval = ((FormulaParsingWorkbook) render).getName(nameStr, sheet);
    return eval.getNameDefinition();
  }
  
  public static String toR1C1String(String formula, int row, int col) {
    //TODO: This might match things in strings...but for my purposes, I don't think that
    //      would change my results?
    StringBuffer newFormula = new StringBuffer();
    
    Matcher match = Pattern.compile("\\$?[A-Z]+\\$?\\d+").matcher(formula);
    while (match.find()) {
      String orig = match.group();
      String[] parts = orig.replaceAll("([A-Z])(\\$?\\d)", "$1 $2").split(" ");
      
      //if (!parts[0].startsWith("$")) {
      String refCol = parts[0];
      boolean absCol = refCol.startsWith("$");
      if (absCol) 
        refCol = refCol.replace("$", "");
      int refColNum = CellReference.convertColStringToIndex(refCol);
      refCol = absCol ? "C" + refColNum : "C[" + (refColNum - col) + "]";
      
      String refRow = parts[1];
      boolean absRow = refRow.startsWith("$");
      if (absRow) 
        refRow = refRow.replace("$", "");
      int refRowNum = Integer.parseInt(refRow);
      refRow = absRow ? "R" + refRowNum : "R[" + (refRowNum - row) + "]";
      
      
      String newRef = refRow + refCol;
      //System.out.println(match.group() + " -> " + newRef + " (" + col + "," + row + ")");
      match.appendReplacement(newFormula, newRef);      
    }      
    
    match.appendTail(newFormula);
    return newFormula.toString();
  }
}