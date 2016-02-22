package core;

import java.util.Stack;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.OperandPtg;
import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.ParenthesisPtg;
import org.apache.poi.ss.formula.ptg.Ptg;

public class Parser {
  public static String parseFormula(String formula, int sheet, FormulaParsingWorkbook parse) 
      throws FormulaParseException, UnsupportedOperationException {
    Ptg[] tokens = null;
    
    if (formula.equals(""))
      throw new UnsupportedOperationException("Formula is an empty string.");
    else if (formula.contains("!'"))
      throw new UnsupportedOperationException("Formula contains illegal single quotes.");
    
    try { 
      tokens = FormulaParser.parse(formula, parse, FormulaType.CELL, sheet);
    } catch (FormulaParseException e) {
      //TODO: define own error?
      //throw new FormulaParseException("parseFormula: nonstandard function detected.");
      throw e;
    }
    
    FormulaRenderingWorkbook render = (FormulaRenderingWorkbook) parse;
    return parseFormula(tokens, render);
  }
  
  /**
   * 
   * @param tokens
   * @param render
   */
  public static String parseFormula(Ptg[] tokens, FormulaRenderingWorkbook render) {
    Stack<String> formula = new Stack<String>();
    
    int i = 0;
    for (Ptg ptg : tokens) {
      //System.out.print(i++ + " ");
      String form = "";
      
      if (ptg instanceof OperationPtg) {        
        form = operationParse(formula, ptg);       
      } else if (ptg instanceof OperandPtg) {           
        form = operandParse(render, ptg);        
      } else if (ptg instanceof ParenthesisPtg) {
        form = parseParen(formula);
      } else if (ptg instanceof AttrPtg) {
        form = parseAttr(formula, ptg);
      } else {              
        form = ptg.toFormulaString();
      }
      
      if (form.compareTo("") == 0) { 
        //System.out.println(); 
        continue; 
      }
      
      //System.out.println(form);        
      formula.push(form);
      
    }
    
    String finalFormula = formula.pop();        
    return finalFormula;
  }

  /**
   * SUM with one area argument counts as AttrPtg instead of FuncPtg so yeah!
   * @param formula
   * @param ptg
   * @return
   */
  private static String parseAttr(Stack<String> formula, Ptg ptg) {
    String form = "",
           formulaStr = ptg.toFormulaString().trim();
    
    if (formulaStr.equalsIgnoreCase("sum")) {
      form = "SUM(" + formula.pop() + ")";
    }
    
    return form;
  }

  /**
   * 
   * @param formula
   * @return
   */
  private static String parseParen(Stack<String> formula) {
    String last = formula.pop();
    last = "(" + last + ")";    
    return last;    
  }

  /**
   * 
   * @param render
   * @param ptg
   * @return
   */
  private static String operandParse(FormulaRenderingWorkbook render, Ptg ptg) {
    String form;
    
    //Name tokens need renderer, others don't.
    if (ptg instanceof NamePtg) {
      NamePtg name = (NamePtg) ptg;
      form = name.toFormulaString(render);
    } else {
      OperandPtg operand = (OperandPtg) ptg;
      form = operand.toFormulaString();
    }
    
    return form;
  }
  
  /**
   * 
   * @param formula
   * @param ptg
   * @return
   */
  private static String operationParse(Stack<String> formula, Ptg ptg) {
    OperationPtg op = (OperationPtg) ptg;
    
    int len = op.getNumberOfOperands();
    String[] operands = new String[len];
    
    //Start from the end, else arguments are filled in backwards.
    for (int i = len - 1; i >= 0; --i) {
      operands[i] = formula.pop();
    }
    
    String form = op.toFormulaString(operands);
    return form;
  }
}