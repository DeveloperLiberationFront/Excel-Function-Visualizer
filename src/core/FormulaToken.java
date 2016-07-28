package core;

import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.BoolPtg;
import org.apache.poi.ss.formula.ptg.ErrPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.apache.poi.ss.util.CellReference;

public class FormulaToken {
  //TODO: See if I can remove representationMode -- the wrap() function is the
  //only thing that uses it.
  protected Mode representationMode;
  protected String tokenStr;
  protected Ptg token;
  private int origLen = Integer.MAX_VALUE;
  
  public enum Mode {
    NO_CHANGE,
    REPLACE,
    R1C1
  }
  
  //TODO: Don't like this blank string possibility, but OperationToken needs a blank super...
  public FormulaToken() {
    this.tokenStr = "";
    this.token = null;
    this.representationMode = Mode.NO_CHANGE;
  }
  
  /**
   * Creates a class that refers to a discrete token within an Excel formula.
   * ex:  SUM(A:A)+IF(B1<B2, B1, B2) -> SUM(), A:A, +, IF(), B1, <, B2, B1, B2 
   * @param tok   The discrete token in the formula, expected to not be an operation
   *              token and thus have no arguments.
   */
  public FormulaToken(Ptg tok, Mode mode) {
    this(tok, new CellReference(0,0), mode);
  }

  public FormulaToken(Ptg tok, CellReference cellReference, Mode mode) {
    this.token = tok;
    this.representationMode = mode;
    
    switch (mode) {
      case NO_CHANGE:
        this.tokenStr = tok.toFormulaString().trim();   
        break;
      case REPLACE:
        this.tokenStr = getTypeString(tok);              
        break;
      case R1C1:
        this.tokenStr = toR1C1String(tok, cellReference);
        break;
      default:
        break;      
    }
  }

  /**
   * Replaces a specific basic type (reference, range, int, string, bool) with a generic
   * string representing it so it can be equated with all other FormulaTokens of the 
   * same type.
   * 
   * @param tok   The same type.
   * @return      A generic string representation for that type.
   */
  private String getTypeString(Ptg tok) {
    String type = "";
    
    if (tok instanceof RefPtgBase) {
      type = "~REF~";
    } else if (tok instanceof AreaPtgBase) {
      type = "~RANGE~";
    } else if (tok instanceof IntPtg || tok instanceof NumberPtg) {
      type = "~NUM~";
    } else if (tok instanceof StringPtg) {
      type = "~STR~";
    } else if (tok instanceof BoolPtg) {
      type = "~BOOL~";
    } else if (tok instanceof ErrPtg) {
      type = "~ERROR~";
    } else {                                                        
      type = "~OTHER~"; //TODO Be more specific?
      System.out.println(tok.toFormulaString() + " " + tok.getClass());
    }
    
    return type;
  }
  
  public String toR1C1String(CellReference cell) {
    return toR1C1String(this.token, cell);
  }
  
  private String toR1C1String(Ptg tok, CellReference cell) {
    String original = tok.toFormulaString();
    String relative = "";
    
    if (tok instanceof AreaPtgBase) {
      String[] limits = original.split(":");
      if (limits.length > 2) {
        try { 
          limits = findHalves(limits, ':'); 
        } catch (UnsupportedOperationException ex) {
          throw new UnsupportedOperationException(ex.getMessage() + ": " + original);
        }
      } else if (limits.length == 1) {
        throw new UnsupportedOperationException("Not a proper area (no colon): " + original);
      }   
      
      String first = limits[0];
      String last = limits[1];
      
      AreaPtgBase area = (AreaPtgBase) tok;
      int firstRow = area.getFirstRow();
      int firstCol = area.getFirstColumn(); 
      boolean firstRowRel = area.isFirstRowRelative();
      boolean firstColRel = area.isFirstColRelative();
      String firstRef = convertToR1C1(firstRow, firstRowRel, firstCol, firstColRel, cell);
      String newFirst = (first.contains("!") ? first.substring(0, first.lastIndexOf('!') + 1) : "")
                         + firstRef;    
      
      int lastRow = area.getLastRow();
      int lastCol = area.getLastColumn(); 
      boolean lastRowRel = area.isLastRowRelative();
      boolean lastColRel = area.isLastColRelative();
      String lastRef = convertToR1C1(lastRow, lastRowRel, lastCol, lastColRel, cell);
      String newLast = (last.contains("!") ? last.substring(0, last.lastIndexOf('!') + 1) : "")
                       + lastRef;
      
      relative = newFirst + ":" + newLast;      
    } else if (tok instanceof RefPtgBase) {     
      RefPtgBase ref = (RefPtgBase) tok;
      int refRow = ref.getRow();
      int refCol = ref.getColumn(); 
      boolean rowRel = ref.isRowRelative();
      boolean colRel = ref.isColRelative();
      String newRef = convertToR1C1(refRow, rowRel, refCol, colRel, cell);
      
      relative = (original.contains("!") ? original.substring(0, original.lastIndexOf('!') 
          + 1) : "") + newRef;
    } else {
      relative = tok.toFormulaString();
    }        
    
    return relative;
  }

  /**
   * Counts the number of times that a character appears in a string.
   * @param first
   * @param c
   * @return
   */
  private int countChar(String first, String c) {
    return first.length() - first.replaceAll(c, "").length();
  }

  /**
   * More than one colon can happen with a 3D reference, when a sheet name has a colon in it.
   * For now, just collapse any elements with only one quote into the next one until they have two.
   * @param limits
   * @param split 
   * @return
   */
  private String[] findHalves(String[] limits, char split) throws UnsupportedOperationException {
    //Expectations: always two halves, never more.
    String[] halves = new String[2];
    String currentHalf = "";
    int currentHalfIndex = 0;
    
    for (int i = 0; i < limits.length; ++i) {
      currentHalf += limits[i];
      //TODO: What if there are suddenly more than 2? What if it doesn't use quotes?
      if (countChar(currentHalf, "'") % 2 == 0) {
        halves[currentHalfIndex] = currentHalf;
        ++currentHalfIndex;
        currentHalf = "";
      } else {
        currentHalf += split;
      }
      
      if (currentHalfIndex == 2 && i < limits.length - 1) {
        throw new UnsupportedOperationException("Unexpected formula construction");
      }
    }    
    
    //If there were unused segments...
    if (currentHalfIndex < 2) {
      throw new UnsupportedOperationException("Unexpected formula construction");
    }
    
    return halves;
  }

  /**
   * Change A1 to R1C1
   * @param row       Row number.
   * @param isRowRel  Whether row is relative or absolute ($)
   * @param col       Column number.
   * @param isColRel  Whether column is relative or absolute ($)
   * @param cell      Originating cell, used for current coordinates.
   * @return          The R1C1 representation of the reference cell from the starting cell.
   */
  private String convertToR1C1(int row, boolean isRowRel, int col, 
      boolean isColRel, CellReference cell) {
    int cellRow = cell.getRow(), cellCol = cell.getCol();
    String formulaCol = isColRel ? "C[" + (col - cellCol) + "]" : "C" + (col+1);
    String formulaRow = isRowRel ? "R[" + (row - cellRow) + "]" : "R" + (row+1);
    return formulaRow + formulaCol;
  }
  
  /**
   * Wrap this token in parenthesis.
   * @return
   */
  public String wrap() {
    if (representationMode != Mode.REPLACE) {
      //Don't want to wrap a single leaf node for viz purposes.
      this.tokenStr = "(" + tokenStr + ")";
    }   
    return tokenStr;
  }
  
  /**
   * @return  An empty array; if it's not an operation, it should have no children.
   */
  public FormulaToken[] getChildren() {
    return new FormulaToken[0];
  }
  
  /**
   * This is to store the length of the original formula turned into tokens. Since the length
   * can differ between how the parser can reconstruct it and what it was originally, I want
   * to store the number with this extra step rather than trying to recalculate.
   * 
   * Mainly so I can pick out the shortest example from the database to display.
   * @param origLen   The length in characters of the original formula.
   */
  public void setOrigLen(int origLen) {
    this.origLen = origLen;
    for (FormulaToken child : getChildren())
      child.setOrigLen(origLen);
  }
  
  public int getOrigLen() {
    return origLen;
  }
  
  public String toString() {
    return tokenStr;
  }
  
  /**
   * Functionally identical to toString in this class (but not in OperationToken)
   * @return    the function name
   */
  public String toSimpleString() {
    return toString();
  }
  
  /**
   * Get a string which conveys the hierarchical nature of the formula.
   * @return
   */
  public String toTreeString() {
    return this.toTreeString(new StringBuilder(), 0).toString();
  }
  
  /**
   * Represent the entire hierarchy of the formula as indented list.
   * @param sb      The stringbuilder which is passed between formula tokens
   *                and compiles the entire string.
   * @param depth   How many levels deep we are into the hierarchy. Determines tabbing.
   * @return        The StringBuilder passed in, now altered.
   */
  protected StringBuilder toTreeString(StringBuilder sb, int depth) {    
    tabs(sb, depth);
    sb.append(this.tokenStr);
    sb.append("\n");
    
    return sb;
  }

  /**
   * Adds the correct tabbing for an element of this depth.
   * @param sb      The stringbuilder compiling all parts of the tree string representation.
   * @param depth   How many levels down into the tree we are right now.
   */
  protected void tabs(StringBuilder sb, int depth) {
    sb.append(depth + ".");
    for (int i = 0; i < depth; ++i) {
      sb.append("....");
    }
  }
  
  /**
   * Equality based on function name equality. Can be compared to either 
   * FormulaToken or FormulaStatsNode.
   * TODO: Is this double equality dangerous?
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    
    if (o instanceof FunctionNode) {
      FunctionNode fsn = (FunctionNode) o;
      return this.tokenStr.equals(fsn.getFunction());
    } else if (o instanceof FormulaToken) {
      FormulaToken ft = (FormulaToken) o;
      return toSimpleString().equals(ft.toSimpleString());
      //Because simple string differs between FormulaToken and OperationToken, 
      //call toSimpleString() instead of tokenStr.
    } 
    
    return false;
  }
}