/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.nodevalue;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.DateTimeStruct;
import com.hp.hpl.jena.sparql.util.StringUtils;

/**
 * Implementation of XQuery/XPath functions and operators.
 * http://www.w3.org/TR/xpath-functions/
 * 
 * @author Andy Seaborne
 */
public class XSDFuncOp
{
    // The choice of "24" is arbitrary but more than 18 as required by F&O 
    private static final int DIVIDE_PRECISION = 24 ;
    // --------------------------------
    // Numeric operations
    // http://www.w3.org/TR/xpath-functions/#op.numeric
    // http://www.w3.org/TR/xpath-functions/#comp.numeric
    
    public static NodeValue add(NodeValue nv1, NodeValue nv2) // F&O numeric-add
    {
        switch (classifyNumeric("add", nv1, nv2))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger(nv1.getInteger().add(nv2.getInteger())) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal(nv1.getDecimal().add(nv2.getDecimal())) ;
            case OP_FLOAT:
                return NodeValue.makeFloat(nv1.getFloat() + nv2.getFloat()) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble(nv1.getDouble() + nv2.getDouble()) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    public static NodeValue subtract(NodeValue nv1, NodeValue nv2) // F&O numeric-subtract
    {
        switch (classifyNumeric("subtract", nv1, nv2))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger(nv1.getInteger().subtract(nv2.getInteger())) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal(nv1.getDecimal().subtract(nv2.getDecimal())) ;
            case OP_FLOAT:
                return NodeValue.makeFloat(nv1.getFloat() - nv2.getFloat()) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble(nv1.getDouble() - nv2.getDouble()) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    public static NodeValue multiply(NodeValue nv1, NodeValue nv2) // F&O numeric-multiply
    {
        switch (classifyNumeric("multiply", nv1, nv2))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger(nv1.getInteger().multiply(nv2.getInteger())) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal(nv1.getDecimal().multiply(nv2.getDecimal())) ;
            case OP_FLOAT:
                return NodeValue.makeFloat(nv1.getFloat() * nv2.getFloat()) ;
           case OP_DOUBLE:
                return NodeValue.makeDouble(nv1.getDouble() * nv2.getDouble()) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    // Java 1.4 does not have BigDecimal.ZERO
    private static final BigDecimal BigDecimalZero = new BigDecimal(0e0) ;
    
    /* Quote from XQuery/XPath F&O:
        For xs:float or xs:double values, a positive number divided by positive zero returns INF.
        A negative number divided by positive zero returns -INF.
        Division by negative zero returns -INF and INF, respectively.
        Positive or negative zero divided by positive or negative zero returns NaN.
        Also, INF or -INF divided by INF or -INF returns NaN.
     */
    
    public static NodeValue divide(NodeValue nv1, NodeValue nv2) // F&O numeric-divide
    {
        switch (classifyNumeric("divide", nv1, nv2))
        {
            case OP_INTEGER:
            {
                if ( nv2.getInteger().equals(BigInteger.ZERO) )
                    throw new ExprEvalException("Divide by zero in divide") ;
               // Note: result is a decimal
                BigDecimal d1 = new BigDecimal(nv1.getInteger()) ;
                BigDecimal d2 = new BigDecimal(nv2.getInteger()) ;
                return NodeValue.makeDecimal(decimalDivide(d1, d2)) ;
            }
            case OP_DECIMAL:
            {
                if ( nv2.getDecimal().equals(BigDecimalZero) )
                    throw new ExprEvalException("Divide by zero in decimal divide") ;
                BigDecimal d1 = nv1.getDecimal() ;
                BigDecimal d2 = nv2.getDecimal() ;
                return NodeValue.makeDecimal(decimalDivide(d1, d2)) ;
            }
            case OP_FLOAT:
                // No need to check for divide by zero
                return NodeValue.makeFloat(nv1.getFloat() / nv2.getFloat()) ;
            case OP_DOUBLE:
                // No need to check for divide by zero
                return NodeValue.makeDouble(nv1.getDouble() / nv2.getDouble()) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    private static BigDecimal decimalDivide(BigDecimal d1, BigDecimal d2)
    {
        // Java 1.5-ism BigDecimal.divide(BigDecimal) -- but fails for 1/3 anyway.
        // return d1.divide(d2) ;
        // The one downside here is that the precision is always extended 
        // even when unnecessary.
        try {
            return d1.divide(d2, DIVIDE_PRECISION, BigDecimal.ROUND_FLOOR) ;
        } catch (ArithmeticException ex)
        {
            ALog.warn(XSDFuncOp.class, "ArithmeticException in decimal divide - attempting to treat as doubles") ;
            return new BigDecimal(d1.doubleValue()/d2.doubleValue()) ;
        }
    }
    
    public static NodeValue max(NodeValue nv1, NodeValue nv2)
    {
        int x = compareNumeric(nv1, nv2) ;
        if ( x == Expr.CMP_LESS)
            return nv2 ;
        return nv1 ;
    }

    public static NodeValue min(NodeValue nv1, NodeValue nv2)
    {
        int x = compareNumeric(nv1, nv2) ;
        if ( x == Expr.CMP_GREATER)
            return nv2 ;
        return nv1 ;
    }

    public static NodeValue not(NodeValue nv) // F&O fn:not
    {
        boolean b = XSDFuncOp.booleanEffectiveValue(nv) ; 
        return NodeValue.booleanReturn(!b) ;
    }
    
    
    public static NodeValue booleanEffectiveValueAsNodeValue(NodeValue nv) // F&O fn:boolean
    {
        if ( nv.isBoolean() )   // "Optimization" (saves on object churn)
            return nv ;
        return NodeValue.booleanReturn(booleanEffectiveValue(nv)) ; 
    }
    
    public static boolean booleanEffectiveValue(NodeValue nv) // F&O fn:boolean
    {
        // Apply the "boolean effective value" rules
        //boolean: value of the boolean (strictly, if derived from xsd:boolean) 
        //string: length(string) > 0
        //numeric: number != Nan && number != 0
        // http://www.w3.org/TR/xquery/#dt-ebv
        
        if ( nv.isBoolean() )
            return nv.getBoolean() ;
        if ( nv.isString() )
            return nv.getString().length() > 0 ;
        if ( nv.isInteger() )
            return ! nv.getInteger().equals(NodeValue.IntegerZERO) ;
        if ( nv.isDecimal() )
            return ! nv.getDecimal().equals(NodeValue.DecimalZERO) ;
        if ( nv.isDouble() )
            return nv.getDouble() != 0.0 ;
        NodeValue.raise(new ExprEvalException("Not a boolean effective value (wrong type): "+nv)) ;
        // Does not return
        return false ;
    }

    public static NodeValue unaryMinus(NodeValue nv) // F&O numeric-unary-minus
    {
        switch (classifyNumeric("unaryMinus", nv))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger( nv.getInteger().negate() ) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal( nv.getDecimal().negate() ) ;
            case OP_FLOAT:
                return NodeValue.makeFloat( - nv.getFloat() ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( - nv.getDouble() ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+nv) ;   
        }
    }
    
    public static NodeValue unaryPlus(NodeValue nv) // F&O numeric-unary-plus
    {
        // Not quite a no-op - tests for a number
        int opType = classifyNumeric("unaryPlus", nv) ;
        return nv ;
    }

    public static NodeValue abs(NodeValue nv)
    {
        switch (classifyNumeric("abs", nv))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger(nv.getInteger().abs()) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal(nv.getDecimal().abs()) ;
            case OP_FLOAT:
                return NodeValue.makeFloat( Math.abs(nv.getFloat()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.abs(nv.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+nv) ;   
        }
    }

    public static NodeValue ceiling(NodeValue v)
    {
        switch (classifyNumeric("ceiling", v))
        {
            case OP_INTEGER:
                return v ;
            case OP_DECIMAL:
                BigDecimal dec = v.getDecimal().setScale( 0, BigDecimal.ROUND_CEILING) ;
                return NodeValue.makeDecimal(dec) ;
            case OP_FLOAT:
                // NB - returns a double (no Java Math.ceil(float))
                return NodeValue.makeDouble( Math.ceil(v.getFloat()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.ceil(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;   
        }
    }
    
    public static NodeValue floor(NodeValue v)
    {
        switch (classifyNumeric("floor", v))
        {
            case OP_INTEGER:
                return v ;
            case OP_DECIMAL:
                BigDecimal dec = v.getDecimal().setScale(0, BigDecimal.ROUND_FLOOR) ;
                return NodeValue.makeDecimal(dec) ;
            case OP_FLOAT:
                // NB - returns a double (no Java Math.floor(float)) 
                return NodeValue.makeDouble( Math.floor(v.getFloat()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.floor(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;   
        }
    }

    public static NodeValue round(NodeValue v)
    {
        switch (classifyNumeric("round", v))
        {
            case OP_INTEGER:
                return v ;
            case OP_DECIMAL:
                BigDecimal dec = v.getDecimal().setScale(0, BigDecimal.ROUND_DOWN ) ;
                return NodeValue.makeDecimal(dec) ;
            case OP_FLOAT:
                return NodeValue.makeFloat( Math.round(v.getFloat()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.round(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;   
        }
    }
    
    public static NodeValue sqrt(NodeValue v)
    {
        switch (classifyNumeric("sqrt", v))
        {
            case OP_INTEGER:
                return v ;
            case OP_DECIMAL:
                double dec = v.getDecimal().doubleValue() ;
                return NodeValue.makeDecimal( Math.sqrt(dec) ) ;
            case OP_FLOAT:
                // NB - returns a double
                return NodeValue.makeDouble( Math.sqrt(v.getDouble()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.sqrt(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;   
        }
    }

    /** String operations */

    public static NodeValue stringLength(NodeValue str)
    {
        return NodeValue.makeInteger(str.getString().length()) ;
    }
    
    // NB Java string start from zero and uses start/end
    // F&O strings start from one and uses start/length

    public static NodeValue javaSubstring(NodeValue v1, NodeValue v2)
    {
        return javaSubstring(v1, v2, null) ; 
    }

    public static NodeValue javaSubstring(NodeValue nvString, NodeValue nvStart, NodeValue nvFinish)
    {
        try {
            String string = nvString.getString() ;
            int start = nvStart.getInteger().intValue() ;
            if ( nvFinish == null )
                return NodeValue.makeString(string.substring(start)) ;
            
            int finish = nvFinish.getInteger().intValue() ;
            return NodeValue.makeString(string.substring(start, finish)) ;
        } catch (IndexOutOfBoundsException ex)
        {
            throw new ExprEvalException("IndexOutOfBounds", ex) ;
        }
    }
    

    
    public static NodeValue substring(NodeValue v1, NodeValue v2)
    {
        return substring(v1, v2, null) ; 
    }

    public static NodeValue substring(NodeValue nvString, NodeValue nvStart, NodeValue nvLength)
    {
        try {
            String string = nvString.getString() ;
            int start = intValueStr(nvStart) ;
            if ( start <= 0 )
                start = 1 ;
            start-- ; // F&O strings are one-based ; convert to java. 
            
            if ( nvLength == null )
                return NodeValue.makeString(string.substring(start)) ;
            
            int length = intValueStr(nvLength) ;
            int finish = start + length ;
            if ( finish > string.length() )
                finish = string.length() ; // Java index must be within bounds.
            if ( finish < 0 )
                finish = 0 ;
            
            return NodeValue.makeString(string.substring(start, finish)) ;
        } catch (IndexOutOfBoundsException ex)
        {
            throw new ExprEvalException("IndexOutOfBounds", ex) ;
        }
    }
    
    private static int intValueStr(NodeValue nv) 
    {
        if ( nv.isInteger() ) return nv.getInteger().intValue() ;
        if ( nv.isDecimal() )
            // No decimal round in Java 1.4
            return (int)Math.round(nv.getDecimal().doubleValue()) ;
        
        if ( nv.isFloat()   ) return Math.round(nv.getFloat()) ;
        if ( nv.isDouble()  ) return (int)Math.round(nv.getDouble()) ;
        throw new ExprEvalException("Not a number:"+nv) ;
    }
    
    public static NodeValue strContains(NodeValue string, NodeValue match)
    {
        strCheck(string, match) ;
        // Indirect to centralise a Java 1.5ism.
        boolean x = StringUtils.contains(string.getString(), match.getString()) ;
        return NodeValue.booleanReturn(x) ;
    }
    
    public static NodeValue strStartsWith(NodeValue string, NodeValue match)
    {
        strCheck(string, match) ;
        return NodeValue.booleanReturn(string.getString().startsWith(match.getString())) ;
    }
    
    public static NodeValue strEndsWith(NodeValue string, NodeValue match)
    {
        strCheck(string, match) ;
        return NodeValue.booleanReturn(string.getString().endsWith(match.getString())) ;
    }

    public static NodeValue strLowerCase(NodeValue string)
    {
        strCheck(string) ;
        return NodeValue.makeString(string.getString().toLowerCase()) ;
    }

    public static NodeValue strUpperCase(NodeValue string)
    {
        strCheck(string) ;
        return NodeValue.makeString(string.getString().toUpperCase()) ;
    }

    // .getString does this anyway.
//    private static void strCheck(NodeValue str)
//    {
//        if ( !str.isString() )
//            throw new ExprEvalException("Not a string: "+str) ;
//    }

    private static void strCheck(NodeValue str)
    {
        if ( !str.isString() )
            throw new ExprEvalException("Not a string: "+str) ;
    }
    
    private static void strCheck(NodeValue str1, NodeValue str2)
    {
        if ( !str1.isString() )
            throw new ExprEvalException("Not a string (first arg): "+str1) ;
        if ( !str2.isString() )
            throw new ExprEvalException("Not a string (second arg): "+str2) ;
    }
    
    private static final int OP_INTEGER  = 10 ;
    private static final int OP_DECIMAL  = 20 ;
    private static final int OP_DOUBLE   = 30 ;
    private static final int OP_FLOAT    = 40 ;
    
    // Classify the input arguments
    // Preference is integer > decimal > float> double
    // Note the need for coordination with NodeValueTYPE isTYPE and getTYPE operations.
    
    public static int classifyNumeric(String fName, NodeValue nv1, NodeValue nv2)
    {
        if ( !nv1.isNumber() )
            throw new ExprEvalException("Not a number (first arg to "+fName+"): "+nv1) ;
        if ( !nv2.isNumber() )
            throw new ExprEvalException("Not a number (second arg to "+fName+"): "+nv2) ;
        
        if ( nv1.isInteger() )
        {
            if (nv2.isInteger() )
                return OP_INTEGER ;
            if ( nv2.isDecimal() )
                return OP_DECIMAL ;
            if ( nv2.isFloat() )
                return OP_FLOAT ;
            if ( nv2.isDouble() )
                return OP_DOUBLE ;
            throw new ARQInternalErrorException("Numeric op unrecognized (second arg to "+fName+"): "+nv2) ;
        }
        
        if ( nv1.isDecimal() )
        {
            if ( nv2.isDecimal() )
                return OP_DECIMAL ;
            if ( nv2.isFloat() )
                return OP_FLOAT ;
            if ( nv2.isDouble() )
                return OP_DOUBLE ;
            throw new ARQInternalErrorException("Numeric op unrecognized (second arg to "+fName+"): "+nv2) ;
        }

        if ( nv1.isFloat() )
        {
            if ( nv2.isFloat() )
                return OP_FLOAT ;
            if ( nv2.isDouble() )
                return OP_DOUBLE ;
            throw new ARQInternalErrorException("Numeric op unrecognized (second arg to "+fName+"): "+nv2) ;
        }            
        
        if ( nv1.isDouble() )
        {
            if ( nv2.isDouble() )
                return OP_DOUBLE ;
            throw new ARQInternalErrorException("Numeric op unrecognized (second arg to "+fName+"): "+nv2) ;
        }
            
        throw new ARQInternalErrorException("Numeric op unrecognized (first arg to "+fName+"): "+nv1) ;
    }
    
    public static int classifyNumeric(String fName, NodeValue nv)
    {
        if ( ! nv.isNumber() )
            throw new ExprEvalException("Not a number: ("+fName+") "+nv) ;
        if ( nv.isInteger() )
            return OP_INTEGER ;
        if ( nv.isDecimal() )
            return OP_DECIMAL ;
        if ( nv.isFloat() )
            return OP_FLOAT ;
        if ( nv.isDouble() )
            return OP_DOUBLE ;
        throw new ARQInternalErrorException("Numeric op unrecognized ("+fName+"): "+nv) ;
    }
    // --------------------------------
    // Comparisons operations
    // Do not confuse with sameValueAs/notSamevalueAs
    
    private static int calcReturn(int x)
    {
        if ( x < 0 ) return Expr.CMP_LESS ;
        if ( x > 0 ) return Expr.CMP_GREATER ;
        return Expr.CMP_EQUAL ;
    }
    
    
    public static int compareNumeric(NodeValue nv1, NodeValue nv2)
    {
        int opType = classifyNumeric("compareNumeric", nv1, nv2) ;

        switch (opType)
        {
            case OP_INTEGER:
                return calcReturn(nv1.getInteger().compareTo(nv2.getInteger())) ;
            case OP_DECIMAL:
                return calcReturn(nv1.getDecimal().compareTo(nv2.getDecimal())) ;
            case OP_FLOAT:
                return calcReturn(Float.compare(nv1.getFloat(), nv2.getFloat())) ;
            case OP_DOUBLE:
                return calcReturn(Double.compare(nv1.getDouble(), nv2.getDouble())) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    //public static int compareDatetime(NodeValue nv1, NodeValue nv2) 
    
    // --------------------------------
    // Functions on strings
    // http://www.w3.org/TR/xpath-functions/#d1e2222
    // http://www.w3.org/TR/xpath-functions/#substring.functions
    
    // String operations
    //  stringCompare = fn:compare
    //  fn:length
    //  fn:string-concat
    //  fn:substring
    // langMatch
    
    public static int compareString(NodeValue nv1, NodeValue nv2)
    { 
        return calcReturn(nv1.getString().compareTo(nv2.getString())) ;
    }
        
    
    // --------------------------------
    // Date/DateTime operations
    // http://www.w3.org/TR/xpath-functions/#comp.duration.datetime
    //  dateTimeCompare
    // works for dates as well because they are implemented as dateTimes on their start point.

    /**
     * Under strict F&O, dateTimes and dates with no timezone have one magcially applied. This default tiemzoine is impementation dependent and can lead to different answers to queries depending on the timezone. Normally, ARQ uses XMLSchema dateTime comparions, which an yield "indeterminate", which in turn is an evaluation error. F&O insists on true/false so can lkead to false positves and negatives. 
     */
    public static boolean strictDateTimeFO = false ;

    public static int compareDateTime(NodeValue nv1, NodeValue nv2)
    { 
        if ( strictDateTimeFO )
            return compareDateTimeFO(nv1, nv2) ;
        return compareXSDDateTime(nv1.getDateTime(), nv2.getDateTime()) ;
    }

    public static int compareDate(NodeValue nv1, NodeValue nv2)
    { 
        if ( strictDateTimeFO )
            return compareDateFO(nv1, nv2) ;
        return compareXSDDateTime(nv1.getDate(), nv2.getDate()) ;
    }

    public static String defaultTimezone = "Z" ;
    
    private static int compareDateTimeFO(NodeValue nv1, NodeValue nv2)
    {
        XSDDateTime dt1 = nv1.getDateTime() ;
        XSDDateTime dt2 = nv2.getDateTime() ;
        
        int x = compareXSDDateTime(dt1, dt2) ;
        
        if ( x == XSDDateTime.INDETERMINATE )
        {
            NodeValue nv3 = fixupDateTime(nv1) ;
            if ( nv3 != null )
            {
                XSDDateTime dt3 = nv3.getDateTime() ; 
                x =  compareXSDDateTime(dt3, dt2) ;
                if ( x == XSDDateTime.INDETERMINATE )
                    throw new ARQInternalErrorException("Still get indeterminate comparison") ;
                return x ;
            }
            
            nv3 = fixupDateTime(nv2) ;
            if ( nv3 != null )
            {
                XSDDateTime dt3 = nv3.getDateTime() ; 
                x =  compareXSDDateTime(dt1, dt3) ;
                if ( x == XSDDateTime.INDETERMINATE )
                    throw new ARQInternalErrorException("Still get indeterminate comparison") ;
                return x ;
            }
            
            throw new ARQInternalErrorException("Failed to fixup dateTimes") ;
        }
        return x ;
        
    }
    
    // This only diffres by some "dateTime" => "date" 
    private static int compareDateFO(NodeValue nv1, NodeValue nv2)
    {
        XSDDateTime dt1 = nv1.getDate() ;
        XSDDateTime dt2 = nv2.getDate() ;

        int x =  compareXSDDateTime(dt1, dt2) ;    // Yes - compareDateTIme
        if ( x == XSDDateTime.INDETERMINATE )
        {
            NodeValue nv3 = fixupDate(nv1) ;
            if ( nv3 != null )
            {
                XSDDateTime dt3 = nv3.getDate() ; 
                x =  compareXSDDateTime(dt3, dt2) ;
                if ( x == XSDDateTime.INDETERMINATE )
                    throw new ARQInternalErrorException("Still get indeterminate comparison") ;
                return x ;
            }
            
            nv3 = fixupDate(nv2) ;
            if ( nv3 != null )
            {
                XSDDateTime dt3 = nv3.getDate() ; 
                x =  compareXSDDateTime(dt1, dt3) ;
                if ( x == XSDDateTime.INDETERMINATE )
                    throw new ARQInternalErrorException("Still get indeterminate comparison") ;
                return x ;
            }
            
            throw new ARQInternalErrorException("Failed to fixup dateTimes") ;
        }
        return x ;
    }
    
    private static NodeValue fixupDateTime(NodeValue nv)
    {
        DateTimeStruct dts = DateTimeStruct.parseDateTime(nv.asNode().getLiteralLexicalForm()) ;
        if ( dts.timezone != null )
            return null ;
        {
            dts.timezone = defaultTimezone ;
            nv = NodeValue.makeDateTime(dts.toString()) ;
            if ( ! nv.isDateTime() )
                throw new ARQInternalErrorException("Failed to reform an xsd:dateTime") ;
            return nv ;
        }
    }
    
    private static NodeValue fixupDate(NodeValue nv)
    {
        DateTimeStruct dts = DateTimeStruct.parseDate(nv.asNode().getLiteralLexicalForm()) ;
        if ( dts.timezone != null )
            return null ;
        {
            dts.timezone = defaultTimezone ;
            nv = NodeValue.makeDate(dts.toString()) ;
            if ( ! nv.isDate() )
                throw new ARQInternalErrorException("Failed to reform an xsd:date") ;
            return nv ;
        }
    }

    private static int compareXSDDateTime(XSDDateTime dt1 , XSDDateTime dt2)
    {
        // Returns codes are -1/0/1 but also 2 for "Indeterminate"
        // which occurs when one has a timezone and one does not
        // and they are less then 14 hours apart.
        
        // F&O has an "implicit timezone" - this code implements the XMLSchema compare algorithm.  

        int x = dt1.compare(dt2) ;
        if ( x == XSDDateTime.EQUAL )
            return Expr.CMP_EQUAL ;
        if ( x == XSDDateTime.LESS_THAN )
            return Expr.CMP_LESS ;
        if ( x == XSDDateTime.GREATER_THAN )
            return Expr.CMP_GREATER ;
        if ( x == XSDDateTime.INDETERMINATE )
            return Expr.CMP_INDETERMINATE ;
        throw new ARQInternalErrorException("Unexpected return from XSDDateTime.compare: "+x) ;
    }
    
//    private static int compareCal(Calendar cal1 , Calendar cal2)
//    {
//        if ( cal1.after(cal2) )
//            return Expr.CMP_GREATER ; 
//
//        if ( cal1.before(cal2) )
//            return Expr.CMP_LESS ;
//
//        return Expr.CMP_EQUAL ;
//        // Java 1.5.0 , not Java 1.4.2
//        //return cal1.compareTo(cal2) ;
//    }

    // --------------------------------
    // Boolean operations
    
    /* Logical OR and AND is special with respect to handling errors truth table.
     * AND they take effective boolean values, not boolean 
     * 
    A       B   |   NOT A   A && B  A || B
    -------------------------------------
    E       E   |   E       E       E
    E       T   |   E       E       T
    E       F   |   E       F       E
    T       E   |   F       E       T
    T       T   |   F       T       T
    T       F   |   F       F       T
    F       E   |   T       F       E
    F       T   |   T       F       T
    F       F   |   T       F       F
    */

    // Not possible because of error masking.
    // public static NodeValue logicalOr(NodeValue x, NodeValue y)
    // public static NodeValue logicalAnd(NodeValue x, NodeValue y)
    
    public static int compareBoolean(NodeValue nv1, NodeValue nv2)
    {
        boolean b1 = nv1.getBoolean() ;
        boolean b2 = nv2.getBoolean() ;
        if ( b1 == b2 )
            return Expr.CMP_EQUAL ;
        
        if ( !b1 && b2 )
            return Expr.CMP_LESS ;
        if ( b1 && !b2 )
            return Expr.CMP_GREATER ;
        throw new ARQInternalErrorException("Weird boolean comparison: "+nv1+", "+nv2) ; 
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
