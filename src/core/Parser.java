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
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;

import utils.POIUtils;

public class Parser {
  public static final XSSFEvaluationWorkbook BLANK = XSSFEvaluationWorkbook.create(POIUtils.getWorkbook("./core/utils/sum.xlsx"));

  /**
   * Break down a formula into individual tokens and wrap them up in nested FormulaTokens.
   * @param formula                         Formula to parse.
   * @return                                A FormulaToken which represents the top-level function
   *                                          of the formula and contains all interior FormulaTokens.
   */
  public static FormulaToken parseFormula(String formula) 
      throws FormulaParseException, UnsupportedOperationException {
    return parseFormula(formula, 0, BLANK);
  }
  
  /**
   * Break down a formula into individual tokens and wrap them up in nested FormulaTokens.
   * @param formula                         Formula to parse.
   * @param sheet                           Sheet number of the formula.
   * @param parse                           The workbook in which the formula was found.
   * @return                                A FormulaToken which represents the top-level function
   *                                          of the formula and contains all interior FormulaTokens.
   * @throws FormulaParseException          If Apache POI can't properly parse the formula.
   *                                          (Like if there's a Name used that it can't resolve)
   * @throws UnsupportedOperationException  If the formula is blank or has quotes after exclamation
   *                                          ( !' ), which I saw in a few and it couldn't parse.
   */
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
   * After using POI to parse a formula down into tokens, package them into recursive FormulaTokens.
   * @param tokens  The Ptg tokens from POI.
   * @param render  The rendering workbook which contained the formula.
   */
  public static FormulaToken parseFormula(Ptg[] tokens, FormulaRenderingWorkbook render) {
    Stack<FormulaToken> formula = new Stack<FormulaToken>();
    
    for (Ptg ptg : tokens) {
      FormulaToken form = null;
      
      if (ptg instanceof MemFuncPtg || ptg instanceof MemAreaPtg) {
        continue;   //As per test_16_outermostmissing, MemFuncPtg act as tokens but have no 
                    //representation in the function, pushing the tokens in the stack off by one.
      } else if (ptg instanceof OperationPtg) {        
        form = operationParse(ptg, formula);       
      } else if (ptg instanceof OperandPtg) {           
        form = operandParse(ptg, render);        
      } else if (ptg instanceof ParenthesisPtg) {
        form = parseParen(formula);
      } else if (ptg instanceof AttrPtg) {
        form = parseAttr(ptg, formula);
      } else {              
        form = new FormulaToken(ptg);
      }
      
      if (form == null) { 
        continue; 
      }
      
      formula.push(form);
      
    }
    
    FormulaToken finalFormula = formula.pop();        
    return finalFormula;
  }

  /**
   * SUM with one area argument counts as AttrPtg instead of FuncPtg so yeah!
   * @param ptg
   * @param formula
   * @return
   */
  private static FormulaToken parseAttr(Ptg ptg, Stack<FormulaToken> formula) {
    AttrPtg attr = (AttrPtg) ptg;
    
    FormulaToken form = null;
    String formulaStr = attr.toFormulaString().trim();
    
    if (formulaStr.equalsIgnoreCase("sum")) {
      FormulaToken arg = formula.pop();
      form = new OperationToken(attr, arg);
    }
    
    return form;
  }

  /**
   * Don't represent parentheses as their own node but rather wrap the representation
   * of the most recent node in parentheses.
   * 
   * @param formula   All FormulaTokens so far.
   * @return          The newly-wrapped formula token.
   */
  private static FormulaToken parseParen(Stack<FormulaToken> formula) {
    FormulaToken last = formula.pop();
    last.wrap();    
    return last;    
  }

  /**
   * Wrap operands in FormulaTokens. If the operand is a name, pass in the rendering workbook too.
   * @param ptg     The operand token.
   * @param render  The rendering workbook in which the formula was found.
   * @return        The new formula token.
   */
  private static FormulaToken operandParse(Ptg ptg, FormulaRenderingWorkbook render) {
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
   * Wrap an operation (function) token and make sure it gets its respective arguments.
   * @param ptg     The operation token.
   * @param formula The stack of all tokens so far, which includes the arguments this will need.
   * @return        The new formula token.
   */
  private static FormulaToken operationParse(Ptg ptg, Stack<FormulaToken> formula) {
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