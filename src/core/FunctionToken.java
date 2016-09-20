package core;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.util.CellReference;

public class FunctionToken extends Token {
  private Token[] children;          //The arguments of this function.
  //A string representation of this token without any information about arguments.
  private String op;           
  
  /**
   * An Operation token represents a function in the overall formula which itself takes 
   * other tokens as arguments. For example, IF() can take 2 or 3 arguments: so `tok`
   * is the Ptg token that refers to IF() individually, and `args` is an array of the 
   * tokens that serve as the 2 or 3 arguments. 
   * Arithmetical operations, like + - * /, also count as operations which take arguments.
   * 
   * @param tok   Spreadsheet operation.
   * @param args    All the arguments in the function defined by token.
   */
  public FunctionToken(OperationPtg tok, Token[] args) {
    int len = args.length;
    if (tok.getNumberOfOperands() != len) {
      throw new UnsupportedOperationException("OperationToken: not enough arguments "
          + "to the operation.");
    }   
    
    this.token = tok;
    
    String[] strArgs = Arrays.stream(args).map(s -> s.toString()).toArray(String[]::new);
    this.tokenStr = tok.toFormulaString(strArgs);
    
    String[] origArgs = Arrays.stream(args).map(s -> s.toOrigString()).toArray(String[]::new);
    this.origStr = tok.toFormulaString(origArgs);
    
    this.op = extractOp(tok, len);
        
    addChildren(len, args);
  }
  
  /**
   * Constructor used primarily for single-arg SUM. We expect only one arg.
   * 
   * @param tok   String representation of the function, including arguments.
   * @param args    Individual FormulaToken arguments.
   */
  public FunctionToken(AttrPtg tok, Token arg) {  
    this.token = tok;  
    this.tokenStr = "SUM(" + arg + ")";
    this.op = "SUM()";
    addChildren(1, new Token[] {arg});
  }

  /**
   * Make and populate the children of this token.
   * @param len   Number of expected children for this function.
   * @param args  The array of children to this node.
   */
  private void addChildren(int len, Token[] args) {
    children = new Token[len];
    for (int i = 0; i < len; ++i) {
      children[i] = args[i];
    }
  }
  
  private static final Matcher NULL = Pattern.compile("null,?").matcher("");  

  /**
   * Extracts the operator from the string. Uses an array of blank Strings so all operands
   * are represented as string "null" and thus easier to manipulate.
   * @param func    The operation token.
   * @param len     Number of arguments the argument expects.
   * @return        The simplest string representation of this operation, either just FOO() 
   *                or the binary symbol 
   *                (+-/*& etc)
   */
  private String extractOp(OperationPtg func, int len) {
    String[] nulls = new String[len];
    String funcOp = func.toFormulaString(nulls);
    funcOp = NULL.reset(funcOp).replaceAll("");    
    return funcOp;    
  }

  public String wrap() {
    this.tokenStr = "(" + tokenStr + ")";
    return tokenStr;
  }
  
  public Token[] getChildren() {
    return children;
  }

  public String toString() {
    return tokenStr;
  }
  
  /**
   * Like toString but ignores all the arguments of the function.
   * @return    the function name
   */
  public String toSimpleString() {
    return op;
  }
  
  public String toOrigString() {
    return this.origStr;
  }
  
  /**
   * Builds a hierarchical string of this node and it's children.
   * @param sb    The stringbuilder passed on from a higher level which contains the 
   *              whole string so far.
   * @param depth How many levels down the tree we are now.
   */
  public StringBuilder toTreeString(StringBuilder sb, int depth) {    
    tabs(sb, depth);
    sb.append(this.op);
    sb.append("\n");
    
    for (Token child : children) {
      child.toTreeString(sb, depth + 1);
    }
    
    return sb;
  }
  
  public String toR1C1String(Ptg tok, CellReference cell) {
    String[] strArgs = new String[children.length];
    
    for (int i = 0; i < children.length; ++i) {
      Token child = children[i];
      strArgs[i] = child.toR1C1String(cell);
    }
    
    if (tok instanceof OperationPtg) {
      OperationPtg opTok = (OperationPtg) tok;      
      return opTok.toFormulaString(strArgs);
    } else if (tok instanceof AttrPtg) {
      AttrPtg opTok = (AttrPtg) tok;
      return opTok.toFormulaString(strArgs);
    } else {
      throw new UnsupportedOperationException("Unexpected OperationPtg type when converting "
          + "to R1C1: " + tok.getClass());
    }
  }
}