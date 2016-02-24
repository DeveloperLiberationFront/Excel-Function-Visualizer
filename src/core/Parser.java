package core;

import java.util.Stack;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.MemAreaPtg;
import org.apache.poi.ss.formula.ptg.MemFuncPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.OperandPtg;
import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.ParenthesisPtg;
import org.apache.poi.ss.formula.ptg.Ptg;

public class Parser {
  public static FormulaToken parseFormula(String formula, int sheet, FormulaParsingWorkbook parse) 
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
  public static FormulaToken parseFormula(Ptg[] tokens, FormulaRenderingWorkbook render) {
    Stack<FormulaToken> formula = new Stack<FormulaToken>();
    
    int i = 0;
    for (Ptg ptg : tokens) {
      //System.out.print(i++ + " ");
      FormulaToken form = null;
      
      if (ptg instanceof MemFuncPtg || ptg instanceof MemAreaPtg) {
        continue;   //As per test_16_outermostmissing, MemFuncPtg act as tokens but have no 
                    //representation in the function, pushing the tokens off by one.
      } else if (ptg instanceof OperationPtg) {        
        form = operationParse(formula, ptg);       
      } else if (ptg instanceof OperandPtg) {           
        form = operandParse(render, ptg);        
      } else if (ptg instanceof ParenthesisPtg) {
        form = parseParen(formula);
      } else if (ptg instanceof AttrPtg) {
        form = parseAttr(formula, ptg);
      } else {              
        form = new FormulaToken(ptg);
      }
      
      if (form == null) { 
        //System.out.println(); 
        continue; 
      }
      
      //System.out.println(form);        
      formula.push(form);
      
    }
    
    FormulaToken finalFormula = formula.pop();        
    return finalFormula;
  }

  /**
   * SUM with one area argument counts as AttrPtg instead of FuncPtg so yeah!
   * @param formula
   * @param ptg
   * @return
   */
  private static FormulaToken parseAttr(Stack<FormulaToken> formula, Ptg ptg) {
    FormulaToken form = null;
    String formulaStr = ptg.toFormulaString().trim();
    
    if (formulaStr.equalsIgnoreCase("sum")) {
      FormulaToken arg = formula.pop();
      form = new OperationToken("SUM(" + arg.toString() + ")", arg);
    }
    
    return form;
  }

  /**
   * 
   * @param formula
   * @return
   */
  private static FormulaToken parseParen(Stack<FormulaToken> formula) {
    FormulaToken last = formula.pop();
    last.wrap();    
    return last;    
  }

  /**
   * 
   * @param render
   * @param ptg
   * @return
   */
  private static FormulaToken operandParse(FormulaRenderingWorkbook render, Ptg ptg) {
    FormulaToken form;
    
    //Name tokens need renderer, others don't.
    if (ptg instanceof NamePtg) {
      NamePtg name = (NamePtg) ptg;
      form = new FormulaToken(name, render);
    } else {
      OperandPtg operand = (OperandPtg) ptg;
      form = new FormulaToken(operand);
    }
    
    return form;
  }
  
  /**
   * 
   * @param formula
   * @param ptg
   * @return
   */
  private static FormulaToken operationParse(Stack<FormulaToken> formula, Ptg ptg) {
    OperationPtg op = (OperationPtg) ptg;
    
    int len = op.getNumberOfOperands();
    FormulaToken[] operands = new FormulaToken[len];
    
    //Start from the end, else arguments are filled in backwards.
    for (int i = len - 1; i >= 0; --i) {
      operands[i] = formula.pop();
    }
    
    FormulaToken form = new OperationToken(op, operands);
    return form;
  }
}