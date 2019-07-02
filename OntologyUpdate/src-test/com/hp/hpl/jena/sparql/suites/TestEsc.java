/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import junit.framework.*;

import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.lang.ParserBase;
import com.hp.hpl.jena.sparql.util.Utils;

/** com.hp.hpl.jena.query.test.TestMisc
 * 
 * @author Andy Seaborne
 */

public class TestEsc extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestEsc.class) ;
        ts.setName(Utils.classShortName(TestEsc.class)) ;
        return ts ;
    }
    
    public void testEsc01() { execTest("x\\uabcd", "x\uabcd") ; }
    public void testEsc02() { execTest("\\uabcdx", "\uabcdx") ; }
    public void testEsc03() { execTest("1234\\uabcd1234", "1234\uabcd1234") ; }
    public void testEsc04() { execTestFail("\\X") ; }
    public void testEsc05() { execTestFail("\\Xz") ; }
    public void testEsc06() { execTestFail("a\\X") ; }
    
    
    public void testEscUni01() { execTestFail("\\uabck") ; }
    public void testEscUni02() { execTestFail("\\uab") ; }
    public void testEscUni03() { execTestFail("\\uabc") ; }
    public void testEscUni04() { execTestFail("\\ua") ; }
    public void testEscUni05() { execTestFail("\\u") ; }
    public void testEscUni06() { execTestFail("\\") ; }
    public void testEscUni07() { execTest("\\u0020", " ") ; }
    public void testEscUni08() { execTest("\\uFFFF", "\uFFFF") ; }
    public void testEscUni09() { execTest("\\u0000", "\u0000") ; }
    public void testEscUni10() { execTestFail("\\U0000") ; }
    public void testEscUni11() { execTestFail("\\U0000A") ; }
    public void testEscUni12() { execTestFail("\\U0000AB") ; }
    public void testEscUni13() { execTestFail("\\U0000ABC") ; }
    public void testEscUni14() { execTest("\\U0000ABCD", "\uABCD") ; }
    public void testEscUni15() { execTestFail("\\U0000") ; }
    public void testEscUni16() { execTest("\\U00000000", "\u0000") ; }
    public void testEscUni17() { execTest("x\\tx\\nx\\r", "x\tx\nx\r") ; }
    public void testEscUni18() { execTest("x\\t\\n\\r", "x\t\n\r") ; }
    
    
    
    private void execTestFail(String input)
    {
        try {
            String s = ParserBase.testUnescapeStr(input) ;
            fail("Unescaping succeeded on "+input) ;
        } catch (QueryParseException ex)
        {
            return ;
        }
        
    }
    
    private void execTest(String input, String outcome)
    {
        String result = ParserBase.testUnescapeStr(input) ;
        assertEquals("Unescaped string does not match ("+input+")", outcome, result) ;
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