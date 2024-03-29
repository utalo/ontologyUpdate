/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */


package arq;

import java.io.File;

import junit.framework.TestSuite;
import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModEngine;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.junit.EarlReport;
import com.hp.hpl.jena.sparql.junit.QueryTestSuiteFactory;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner;
import com.hp.hpl.jena.sparql.test.ARQTestSuite;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest;


/** A program to execute query test suites
 * 
 * <pre>
 * Usage: 
 *   [--all]
 *   [--dawg]
 *   <i>testManifest</i>
 *   [ --query <i>query</i> --data <i>data</i> --result <i>result</i> ] -- run one test
 * </pre>
 * 
 * @author  Andy Seaborne, Olaf Hartig
 */

public class qtest extends CmdARQ
{
    protected ArgDecl allDecl =    new ArgDecl(ArgDecl.NoValue, "all") ;
    protected ArgDecl wgDecl =     new ArgDecl(ArgDecl.NoValue, "wg", "dawg") ;
    protected ArgDecl queryDecl =  new ArgDecl(ArgDecl.HasValue, "query") ;
    protected ArgDecl dataDecl =   new ArgDecl(ArgDecl.HasValue, "data") ;
    protected ArgDecl resultDecl = new ArgDecl(ArgDecl.HasValue, "result") ;
    protected ArgDecl earlDecl =   new ArgDecl(ArgDecl.NoValue, "earl") ;
    
    protected ModEngine modEngine = null ;
    
    protected TestSuite suite = null;
    protected boolean execAllTests = false;
    protected boolean execDAWGTests = false;
    protected String testfileAbs = null;
    protected boolean createEarlReport = false;
    
    public static void main (String [] argv)
    {
        try {
            new qtest(argv).mainRun() ;
        }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
    }
    
    public qtest(String[] argv)
    {
        super(argv) ;
        
        modEngine = setModEngine() ;
        addModule(modEngine) ;
        
        getUsage().startCategory("Tests (single query)") ;
        add(queryDecl, "--query", "run the given query") ;
        add(dataDecl, "--data", "data file to be queried") ;
        add(resultDecl, "--result", "file that specifies the expected result") ;
        
        getUsage().startCategory("Tests (built-in tests)") ;
        add(allDecl, "--all", "run all built-in tests") ;
        add(wgDecl, "--dawg", "run working group tests") ;
        
        getUsage().startCategory("Tests (execute test manifest)") ;
        getUsage().addUsage("<manifest>", "run the tests specified in the given manifest") ;
        add(earlDecl, "--earl", "create EARL report") ;
    }
    
    protected ModEngine setModEngine()
    {
        return new ModEngine() ;
    }
    
    protected String getCommandName() { return Utils.className(this) ; }
    
    protected String getSummary() { return getCommandName()+" [ --data=<file> --query=<query> --result=<results> ] | --all | --dawg | <manifest>" ; }
    
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        
        if ( contains(queryDecl) || contains(dataDecl) || contains(resultDecl) )
        {
            if ( ! ( contains(queryDecl) && contains(dataDecl) && contains(resultDecl) ) )
                throw new CmdException("Must give query, data and result to run a single test") ;
            
            String query = getValue(queryDecl) ;
            String data = getValue(dataDecl) ;
            String result = getValue(resultDecl) ;
            
            suite = QueryTestSuiteFactory.make(query, data, result) ;
        }
        else if ( contains(allDecl) )
        {
            execAllTests = true ;
        }
        else if ( contains(wgDecl) )
        {
            execDAWGTests = true ;
        }
        else
        {
            // OK - running a manifest
            
            if ( ! hasPositional() )
                throw new CmdException("No manifest file") ;

            String testfile = getPositionalArg(0) ;
            testfileAbs = IRIResolver.resolveGlobal(testfile) ;
            
            createEarlReport = contains(earlDecl) ;
        }
    }
    
    protected void exec()
    {
        if ( suite != null )
        {
            SimpleTestRunner.runAndReport(suite) ;
        }
        else if ( execAllTests )
        {
            allTests() ;
        }
        else if ( execDAWGTests )
        {
            dawgTests() ;
        }
        else
        {
            // running a manifest
            
            NodeValue.VerboseWarnings = false ;
            E_Function.WarnOnUnknownFunction = false ;
            
            if ( createEarlReport )
                oneManifestEarl(testfileAbs) ;
            else
                oneManifest(testfileAbs) ;
        }
    }
    
    static void oneManifest(String testManifest)
    {
        TestSuite suite = QueryTestSuiteFactory.make(testManifest) ;

        //junit.textui.TestRunner.run(suite) ;
        SimpleTestRunner.runAndReport(suite) ;
    }
    
    static void oneManifestEarl(String testManifest)
    {
        String name =  "ARQ" ;
        String releaseName =  "ARQ2" ;
        String version = "ARQ-2.2-dev" ; //ARQ.VERSION ;
        String homepage = "http://jena.sf.net/ARQ" ;
        
        // Include information later.
        EarlReport report = new EarlReport(name, version, homepage) ;
        QueryTestSuiteFactory.results = report ;
        
        Model model = report.getModel() ;
        model.setNsPrefix("dawg", TestManifest.getURI()) ;
        
        // Update the EARL report. 
        Resource jena = model.createResource()
                    .addProperty(FOAF.homepage, model.createResource("http://jena.sf.net/")) ;
        
        // ARQ is part fo Jena.
        Resource arq = report.getSystem()
                        .addProperty(DCTerms.isPartOf, jena) ;
        
        // Andy wrote the test software (updates the thing being tested as well as they are the same). 
        Resource who = model.createResource(FOAF.Person)
                                .addProperty(FOAF.name, "Andy Seaborne")
                                .addProperty(FOAF.homepage, 
                                             model.createResource("http://www.hpl.hp.com/people/afs")) ;
        
        Resource reporter = report.getReporter() ;
        reporter.addProperty(DC.creator, who) ;

        model.setNsPrefix("doap", DOAP.getURI()) ; 
        model.setNsPrefix("xsd", XSD.getURI()) ;
        
        // DAWG specific stuff.
        Resource system = report.getSystem() ;
        system.addProperty(RDF.type, DOAP.Project) ;
        system.addProperty(DOAP.name, name) ;
        system.addProperty(DOAP.homepage, homepage) ;
        system.addProperty(DOAP.maintainer, who) ;
        
        Resource release = model.createResource(DOAP.Version) ;
        system.addProperty(DOAP.release, release) ;
        
        Node today_node = NodeFactory.todayAsDate() ;
        Literal today = model.createTypedLiteral(today_node.getLiteralLexicalForm(), today_node.getLiteralDatatype()) ;
        release.addProperty(DOAP.created, today) ;
        release.addProperty(DOAP.name, releaseName) ;      // Again
        
        TestSuite suite = QueryTestSuiteFactory.make(testManifest) ;
        SimpleTestRunner.runSilent(suite) ;
        
        QueryTestSuiteFactory.results.getModel().write(System.out, "TTL") ;
        
    }
    
    static void allTests()
    {
        // This should load all the built in tests.
        // Check to see if expected directories are present or not.
        
        ensureDirExists("testing") ;
        ensureDirExists("testing/ARQ") ;
        ensureDirExists("testing/DAWG") ;
        ensureDirExists("testing/DAWG-Approved") ;
        
        TestSuite ts = ARQTestSuite.suite() ;
        junit.textui.TestRunner.run(ts) ;
        //SimpleTestRunner.runAndReport(ts) ;
    }

    static void dawgTests()
    {
        System.err.println("DAWG tests not packaged up yet") ;
    }
    
    static void ensureDirExists(String dirname)
    {
        File f = new File(dirname) ;
        if ( ! f.exists() || !f.isDirectory() )
        {
            System.err.println("Can't find required directory: '"+dirname+"'") ;
            throw new TerminationException(8) ;
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
