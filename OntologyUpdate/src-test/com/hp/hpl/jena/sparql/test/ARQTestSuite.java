/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.test;


import junit.framework.TestSuite;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.junit.QueryTestSuiteFactory;
import com.hp.hpl.jena.sparql.suites.*;
import com.hp.hpl.jena.sparql.suites.optimizer.OptimizerTestSuite;

/**
 * All the ARQ tests 
 * @author		Andy Seaborne
 */

public class ARQTestSuite extends TestSuite
{
    public static final String testDirARQ = "testing/ARQ" ;
    
    static public TestSuite suite()
    {
        // Fiddle around with the config if necessary
        if ( false )
        {
            QueryEngineMain.unregister() ;
            QueryEngineRef.register() ;
        }
        
        TestSuite ts = new ARQTestSuite() ;

        // Internal
        ts.addTest(TS.suite() );

        // Scripted tests for SPARQL
        ts.addTest(QueryTestSuiteFactory.make(testDirARQ+"/manifest-arq.ttl")) ;
      
        // ARQ + Lucene
        ts.addTest(TestLARQ.suite()) ;
      
        // Scripted tests for ARQ features outside SPARQL syntax
        // Currently at end of manifest-arq.ttl
//        ts.addTest(QueryTestSuiteFactory.make(testDirARQ+"/manifest-ext.ttl")) ;
        
        // The DAWG official tests (some may be duplicated in ARQ test suite
        // but this should be the untouched versions)
        ts.addTest(TS_DAWG.suite()) ;
      
        // The RDQL engine ported to ARQ
        ts.addTest(TS_RDQL.suite()) ;
      
        // API
        ts.addTest(TestAPI.suite()) ;
        
        // SPARQL/Update
        ts.addTest(TS_Update.suite()) ;
        
        ts.addTest(TS_SSE.suite()) ;
        
        // The ARQ-Optimizer test suite, Markus Stocker 08/06/2007
        ts.addTest(OptimizerTestSuite.suite()) ;
        
        return ts ;
    }

	private ARQTestSuite()
	{
        super("ARQ");
        ARQ.init() ;
        // Tests should be silent.
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
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
