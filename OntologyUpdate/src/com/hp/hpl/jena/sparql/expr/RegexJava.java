/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.regex.* ;

/** 
 * @author Andy Seaborne
 */

public class RegexJava implements RegexEngine
{
    Pattern regexPattern ;

    public RegexJava(String pattern, String flags)
    {
        regexPattern = makePattern(pattern, flags) ;
    }
    
    public boolean match(String s)
    {
        Matcher m = regexPattern.matcher(s) ;
        return m.find() ;
    }
    
    private Pattern makePattern(String patternStr, String flags)
    {
        try { 
            int mask = 0 ;
            if ( flags != null )
                mask = makeMask(flags) ;
            return Pattern.compile(patternStr, mask) ;
        } 
        catch (PatternSyntaxException pEx)
        { throw new ExprException("Regex: Pattern exception: "+pEx) ; }
    }


    private int makeMask(String modifiers)
    {
        if ( modifiers == null )
            return 0 ;
        
        int newMask = 0 ;
        for ( int i = 0 ; i < modifiers.length() ; i++ )
        {
            switch(modifiers.charAt(i))
            {
                //case 'i' : newMask |= Pattern.CASE_INSENSITIVE;     break ;
                case 'i' : 
                    // Need both (Java 1.4)
                    newMask |= Pattern.UNICODE_CASE ;
                    newMask |= Pattern.CASE_INSENSITIVE;
                    break ;
                case 'm' : newMask |= Pattern.MULTILINE ;           break ;
                case 's' : newMask |= Pattern.DOTALL ;              break ;
                //case 'x' : newMask |= Pattern.;  break ;
                
                default  : throw new ExprException("Illegal flag in regex modifiers: "+modifiers.charAt(i)) ;
            }
        }
        return newMask ;
    }

}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
