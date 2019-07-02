/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import java.io.* ;

import com.hp.hpl.jena.sparql.lang.rdql.*;

import junit.framework.* ;


public class TS_ExprRDQL extends TestSuite
{
	static final String testSetName = "RDQL - Expressions" ;
	
	public static TestSuite suite()
    {
    	return new TS_ExprRDQL(testSetName) ;
    }
    
    private TS_ExprRDQL(String name)
    {
    	super(name) ;
        
        final boolean FAILURE_OK = true ;
        final boolean NO_FAILURE = false ;

        String xsd = "http://www.w3.org/2001/XMLSchema#" ;

        addTest(new TestNumeric("7", 7)) ;
        addTest(new TestNumeric("-3", -3)) ;
        addTest(new TestNumeric("3+4+5", 3+4+5)) ;
        // Test the trees
        addTest(new TestNumeric("(3+4)+5", 3+4+5)) ;
        addTest(new TestNumeric("3+(4+5)", 3+4+5)) ;

        // Precedence
        addTest(new TestNumeric("3*4+5", 3*4+5)) ;
        addTest(new TestNumeric("3*(4+5)", 3*(4+5))) ;

        addTest(new TestNumeric("10-3-5", 10-3-5)) ;
        addTest(new TestNumeric("(10-3)-5", (10-3)-5)) ;
        addTest(new TestNumeric("10-(3-5)", 10-(3-5))) ;
        addTest(new TestNumeric("10-3+5", 10-3+5)) ;
        addTest(new TestNumeric("10-(3+5)", 10-(3+5))) ;

        addTest(new TestNumeric("1<<2", 1<<2)) ;
        addTest(new TestNumeric("1<<2<<2", 1<<2<<2)) ;

        addTest(new TestNumeric("10000>>2", 10000>>2)) ;

        addTest(new TestNumeric("1.5 + 2.5", 1.5+2.5)) ;
        addTest(new TestNumeric("1.5 + 2", 1.5+2)) ;
        
        // Test longs
        // A long is over 32bites signed = +2Gig
        addTest(new TestNumeric("4111222333444", 4111222333444L)) ;
        addTest(new TestNumeric("1234 + 4111222333444", 1234 + 4111222333444L)) ;
        
        // Boolean
        addTest(new TestBoolean("true", NO_FAILURE, true)) ;

        addTest(new TestBoolean("false", NO_FAILURE, false)) ;

        addTest(new TestBoolean("false || true", NO_FAILURE, true)) ;
        addTest(new TestBoolean("false && true", NO_FAILURE, false)) ;

        addTest(new TestBoolean("2 < 3", NO_FAILURE, 2 < 3)) ;
        addTest(new TestBoolean("2 > 3", NO_FAILURE, 2 > 3)) ;
        addTest(new TestBoolean("(2 < 3) && (3<4)", NO_FAILURE, (2 < 3) && (3<4))) ;
        addTest(new TestBoolean("(2 < 3) && (3>=4)", NO_FAILURE, (2 < 3) && (3>=4))) ;
        addTest(new TestBoolean("(2 < 3) || (3>=4)", NO_FAILURE, (2 < 3) || (3>=4))) ;
        addTest(new TestBoolean("2 == 3", NO_FAILURE, 2 == 3)) ;
        
        // Check that strings are coerced if needed
        addTest(new TestBoolean("2 < '3'", false, 2 < 3)) ;

        addTest(new TestBoolean("\"fred\" ne \"joe\"", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"fred\" eq \"joe\"", NO_FAILURE, false )) ;
        addTest(new TestBoolean("\"fred\" eq \"fred\"", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"fred\" eq 'fred'", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"fred\" eq 'fr\\ed'", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"fred\" ne \"fred\"", NO_FAILURE, false )) ;
        
        // Typed literals
        // Same types.
        addTest(new TestBoolean("'fred'^^<type1> eq 'fred'^^<type1>", NO_FAILURE, true )) ;
        addTest(new TestBoolean("'fred'^^<type1> ne 'joe'^^<type1>",  NO_FAILURE, true )) ;
        // Different types.
        addTest(new TestBoolean("'fred'^^<type1> eq 'fred'^^<type2>", NO_FAILURE, false )) ;
        addTest(new TestBoolean("'fred'^^<type1> ne 'fred'^^<type2>", NO_FAILURE, true )) ;
        
        // true: xsd:string is sameValueAs plain (classic) RDF literal
        addTest(new TestBoolean("'fred'^^<"+xsd+"string> eq 'fred'", NO_FAILURE, true )) ;
        // false: parsing created two RDF literals and these are different 
        addTest(new TestBoolean("'fred'^^<type1> eq 'fred'", NO_FAILURE, false )) ;
        addTest(new TestBoolean("'fred'^^<type1> ne 'fred'", NO_FAILURE, true )) ;
        
        // Numerci expessions: ignore typing (compatibility with RDF-99) 
        addTest(new TestBoolean("'21'^^<int> == '21'", NO_FAILURE, true )) ;

        // Escapes in strings
        addTest(new TestBoolean("\"fred\\1\" eq 'fred1'", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"fred2\" eq 'fred\\2'", NO_FAILURE, true )) ;
        addTest(new TestBoolean("'fred\\\\3' ne \"fred3\"", NO_FAILURE, true )) ;


        addTest(new TestBoolean("\"urn:fred\" eq <urn:fred>", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"urn:fred\" ne <urn:fred>", NO_FAILURE, false )) ;

        addTest(new TestBoolean("\"urn:fred/1.5\" ne <urn:fred/1.5>", NO_FAILURE, false )) ;
        
        addTest(new TestBoolean("\"aabbcc\" =~ /abbc/", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"aabbcc\" =~ /a..c/", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"aabbcc\" =~ /^aabb/", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"aabbcc\" =~ /cc$/", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"aabbcc\" !~ /abbc/", NO_FAILURE, false )) ;
        
        addTest(new TestBoolean("\"aab*bcc\" =~ /ab\\*bc/", NO_FAILURE, true )) ;
        addTest(new TestBoolean("\"aabbcc\" ~~ /ab\\\\*bc/", NO_FAILURE, true )) ;
        addTest(new TestBoolean("'aabbcc' =~ /B.*B/i", NO_FAILURE, true )) ;

        addTest(new TestBoolean("1.5 < 2", NO_FAILURE, 1.5 < 2 )) ;
        addTest(new TestBoolean("1.5 > 2", NO_FAILURE, 1.5 > 2 )) ;
        addTest(new TestBoolean("1.5 < 2.3", NO_FAILURE, 1.5 < 2.3 )) ;
        addTest(new TestBoolean("1.5 > 2.3", NO_FAILURE, 1.5 > 2.3 )) ;
        
        // Longs
        addTest(new TestBoolean("4111222333444 > 1234", NO_FAILURE, 4111222333444L > 1234)) ;
        addTest(new TestBoolean("4111222333444 < 1234", NO_FAILURE, 4111222333444L < 1234L)) ;
        
        // These are false because a failure should occur

        addTest(new TestBoolean("2 < \"fred\"", FAILURE_OK, false)) ;
        addTest(new TestBoolean("2 || true", FAILURE_OK, false)) ;
    }


    public static class TestNumeric extends TestCase
    {
        String s ;
        boolean isDouble = false ;
        long rightAnswer ;
        double rightAnswerDouble ;

        public TestNumeric(String _s, long _rightAnswer)
        {
            super("Numeric test : "+_s+" ") ;
            s = _s ;
            rightAnswer = _rightAnswer ;
            isDouble = false ;
        }

        public TestNumeric(String _s, double _rightAnswer)
        {
            super("Numeric test : "+_s+" ") ;
            s = _s ;
            rightAnswerDouble = _rightAnswer ;
            isDouble = true ;
        }

        protected void runTest() throws Throwable
        {
            ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes()) ;
            RDQLParser parser = new RDQLParser(in) ;

            //parser.CompilationUnit();
            //System.out.println("Input: "+s);

            try {
                parser.Expression() ;
                // Be careful, catch ASAP - JUnit uses errors internally
            }
            catch (Error e)
            {
                fail("Error thrown in parse: "+e) ;
            }

            //parser.top().dump(" ");
            parser.top().postParse2(null) ;
            ExprRDQL e = (ExprRDQL)parser.top() ;
            

            assertTrue("Expression is not ExprNumeric: "+e.getClass().getName() ,
                       (e instanceof ExprNumeric)) ;

            ExprNumeric n = (ExprNumeric)e ;

            RDQL_NodeValue v = n.evalRDQL(null, null) ;

            if ( ! isDouble )
                assertEquals(s+" => "+v.getInt()+" ["+rightAnswer+"]",  v.getInt(), rightAnswer ) ;
            else
                assertEquals(s+" => "+v.getDouble()+" ["+rightAnswerDouble+"]",  v.getDouble(), rightAnswerDouble, 0.0001 ) ;
        }
    } // End of inner class


    public static class TestBoolean extends TestCase
    {
        String s ;
        boolean failureCorrect ;
        boolean rightAnswer ;

        public TestBoolean(String _s, boolean _failureCorrect, boolean _rightAnswer)
        {
            super("Boolean test : "+_s+" ") ;
            s = _s ;
            failureCorrect = _failureCorrect ;
            rightAnswer = _rightAnswer ;
        }

        protected void runTest() throws Throwable
        {
            ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes()) ;
            RDQLParser parser = new RDQLParser(in) ;

            try {
                parser.Expression() ;
            } catch (Error e)
            {
                fail("Error throw in parse: "+s) ;
            }

            parser.top().postParse2(null) ;
            ExprRDQL e = (ExprRDQL)parser.top() ;

            assertTrue("Expression is not ExprBoolean: "+e.getClass().getName(),
                       (e instanceof ExprBoolean) ) ;

            ExprBoolean n = (ExprBoolean)e ;

            RDQL_NodeValue v = null ;
            boolean result = false ;
            try {
                v = n.evalRDQL(null, null) ;
                result = v.getBoolean() ;
            } catch (com.hp.hpl.jena.sparql.lang.rdql.RDQLEvalFailureException evalEx)
            {
                if ( ! failureCorrect )
                    throw evalEx ;
                result = false ;

            }
            assertEquals(s+" => "+result+" ["+rightAnswer+"]", result, rightAnswer ) ;
        }
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
