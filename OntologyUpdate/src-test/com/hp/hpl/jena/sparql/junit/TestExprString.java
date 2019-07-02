/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;



public class TestExprString extends TestExpr
{
    String rightAnswer ;
    
    public TestExprString(String exprStr, String rightAnswer)
    {
        this(exprStr, rightAnswer, null, null, TestExpr.NO_FAILURE) ;
    }

    public TestExprString(String exprStr, int failureMode)
    {
        this(exprStr, null, null, null, failureMode) ;
    }

    public TestExprString(String exprStr, String rightAnswer, int failureMode)
    {
        this(exprStr, rightAnswer, null, null, failureMode) ;
    }
    

    
    public TestExprString(String exprStr, String _rightAnswer, Query query, Binding env, int failureMode)
    {
        super("String test : "+exprStr, exprStr, query, env, failureMode) ;
        this.rightAnswer = _rightAnswer ;
    }
    
    
    void checkExpr(Expr expr) { }

    void checkValue(Expr expr, NodeValue nodeValue)
    {
        if ( super.failureMode == TestExpr.EVAL_FAIL )
            fail(exprString+" => "+expr+" :: Expected eval exception but got: "+nodeValue) ;
        String result = nodeValue.getString() ;
        assertEquals(exprString+" => "+expr+" ", rightAnswer, result ) ;
    }
    
    void checkException(Expr expr, Exception ex)
    {
        if ( !failureCorrect() )
            fail(exprString+" => "+expr+" :: Exception: "+ex) ;
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