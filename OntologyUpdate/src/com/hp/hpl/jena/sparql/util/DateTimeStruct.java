/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parse an xsd:dateTime or xsd:date lexical form */  

public class DateTimeStruct
{
    public boolean xsdDateTime  ;
    public String neg = null ;         // Null if none. 
    public String year = null ;
    public String month = null ;
    public String day = null ;
    public String hour = null ;
    public String minute = null ;
    public String second = null ;      // Inc. fractional parts
    public String timezone = null ;    // Null if none.

    private DateTimeStruct() {}
    
    public static class DateTimeParseException extends RuntimeException
    {}
    
    public String toString()
    { 
        String ySep = "-" ;
        String tSep = ":" ;
        String x = year+ySep+month+ySep+day ;
        if ( xsdDateTime )
            x = x + "T"+hour+tSep+minute+tSep+second ;
        if ( neg != null )
            x = neg+x ;
        if ( timezone != null )
            x = x+timezone ;
        return x ; 
    }
    
    public static final String regexDate        = "(-?)(\\d{4})-(\\d{2})-(\\d{2})" ;
    public static final String regexTime        = "(\\d{2}):(\\d{2}):(\\d{2}(?:\\.\\d+)?)" ;
    public static final String regexTZ          = "(Z|(?:(?:\\+|-)\\d{2}:\\d{2}))?" ;
    
    public static final String regexXSDDateTime = regexDate+"T"+regexTime+regexTZ ;
    public static final String regexXSDDate     = regexDate+regexTZ ;
    
    public static final Pattern patternDateTime = Pattern.compile(regexXSDDateTime);
    public static final Pattern patternDate     = Pattern.compile(regexXSDDate);
    
    public static DateTimeStruct parseDate(String str)
    {
        Matcher m = patternDate.matcher(str) ;

        if ( ! m.matches() ) 
            throw new DateTimeParseException() ;

        DateTimeStruct dt = new DateTimeStruct() ;
        dt.xsdDateTime = false ;
        dt.neg      = matchStrOrNull(str, m, 1) ;
        dt.year     = matchStr(str, m, 2) ;
        dt.month    = matchStr(str, m, 3) ;
        dt.day      = matchStr(str, m, 4) ;
        dt.timezone = matchStrOrNull(str, m, 5) ;
        
        // Because comparisons are based on xsd;ddate start point. 
        dt.hour = "00" ;
        dt.minute = "00" ;
        dt.second = "00" ;
        
        return dt ;
    }
    
    public static DateTimeStruct parseDateTime(String str)
    {
        Matcher m = patternDateTime.matcher(str) ;

        if ( ! m.matches() ) 
            throw new DateTimeParseException() ;

        DateTimeStruct dt = new DateTimeStruct() ;
        dt.xsdDateTime = true ;
        
        dt.neg      = matchStrOrNull(str, m, 1) ;
        dt.year     = matchStr(str, m, 2) ;
        dt.month    = matchStr(str, m, 3) ;
        dt.day      = matchStr(str, m, 4) ;
        dt.hour     = matchStr(str, m, 5) ;
        dt.minute   = matchStr(str, m, 6) ;
        dt.second   = matchStr(str, m, 7) ;
        dt.timezone = matchStrOrNull(str, m, 8) ;
        return dt ;
    }

    private static String matchStr(String str, Matcher m, int i)
    {
        if ( m.start(i) == -1 ) return "" ;
        return str.substring(m.start(i), m.end(i)) ;
    }
    
    private static String matchStrOrNull(String str, Matcher m, int i)
    {
        if ( m.start(i) == -1 ) return null ;
        String s = str.substring(m.start(i), m.end(i)) ;
        if ( s.length() == 0 ) return null ;
        return s ;
    }
    

//    // --------
//    // Alternative parser.  Enables better error messages.
//    private static DateTimeStruct _parseDateTime(String str)
//    {
//        // -? YYYY-MM-DD T hh:mm:ss.ss TZ
//        DateTimeStruct DateTimeParser = new DateTimeStruct() ;
//        int idx = 0 ;
//        
//        if ( str.startsWith("-") )
//        {
//            DateTimeParser.neg = "-" ;
//            idx = 1 ;
//        }
//        
//        // ---- Year-Month-Day
//        DateTimeParser.year = getDigits(4, str, idx) ;
//        idx += 4 ;
//        check(str, idx, '-') ;
//        idx += 1 ;
//        
//        DateTimeParser.month = getDigits(2, str, idx) ;
//        idx += 2 ;
//        check(str, idx, '-') ;
//        idx += 1 ;
//        
//        DateTimeParser.day = getDigits(2, str, idx) ;
//        idx += 2 ;
//        // ---- 
//        check(str, idx, 'T') ;
//        idx += 1 ;
//        
//        // ---- 
//        // Hour-minute-seconds
//        DateTimeParser.hour = getDigits(2, str, idx) ;
//        idx += 2 ;
//        check(str, idx, ':') ;
//        idx += 1 ;
//        
//        DateTimeParser.minute = getDigits(2, str, idx) ;
//        idx += 2 ;
//        check(str, idx, ':') ;
//        idx += 1 ;
//        
//        // seconds
//        DateTimeParser.second = getDigits(2, str, idx) ;
//        idx += 2 ;
//        if ( idx < str.length() && str.charAt(idx) == '.' )
//        {
//            idx += 1 ;
//            int idx2 = idx ;
//            for ( ; idx2 < str.length() ; idx2++ )
//            {
//                char ch = str.charAt(idx) ;
//                if ( ! Character.isDigit(ch) )
//                    break ;
//            }
//            if ( idx == idx2 )
//                throw new DateTimeParseException() ;
//            DateTimeParser.second = DateTimeParser.second+'.'+str.substring(idx, idx2) ;
//            idx = idx2 ;
//        }
//
//        // timezone. Z or +/- 00:00
//        
//        if ( idx < str.length() )
//        {
//            if ( str.charAt(idx) == 'Z' )
//            {
//                DateTimeParser.timezone = "Z" ;
//                idx += 1 ;
//            }
//            else
//            {
//                boolean signPlus = false ;
//                if ( str.charAt(idx) == '+' )
//                    signPlus = true ;
//                else if ( str.charAt(idx) == '-' )
//                    signPlus = false ;
//                else
//                    throw new DateTimeParseException() ;
//                DateTimeParser.timezone = getDigits(2, str, idx) ;
//                check(str, idx, ':') ;
//                DateTimeParser.timezone = DateTimeParser.timezone+':'+getDigits(2, str, idx) ;
//                idx += 5 ;
//                 
//            }
//        }
//        
//        if ( idx != str.length() )
//            throw new DateTimeParseException() ;
//        return DateTimeParser ;
//    }
//
//    private static String getDigits(int num, String string, int start)
//    {
//        for ( int i = start ; i < (start+num) ; i++ )
//        {
//            char ch = string.charAt(i) ;
//            if ( ! Character.isDigit(ch) )
//                throw new DateTimeParseException() ;
//            continue ;
//        }
//        return string.substring(start, start+num) ;
//    }
//    
//    private static void check(String string, int start, char x)
//    {
//        if ( string.charAt(start) != x ) 
//            throw new DateTimeParseException() ;
//    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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