/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;


import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.resultset.PlainFormat;
import com.hp.hpl.jena.sparql.resultset.ResultSetApply;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab;

import com.hp.hpl.jena.query.*;

/** Some utilities for query processing. 
 * 
 * @author Andy Seaborne
 */
public class QueryExecUtils
{
    protected static PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        globalPrefixMap.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;

        globalPrefixMap.setNsPrefix("rs",  ResultSetGraphVocab.getURI()) ;
        //globalPrefixMap.setNsPrefix("owl" , ARQConstants.owlPrefix) ;
    }

    public static void executeQuery(Query query, QueryExecution queryExecution)
    {
        executeQuery(query,  queryExecution, ResultsFormat.FMT_TEXT) ;
    }

    public static void executeQuery(Query query, QueryExecution queryExecution, ResultsFormat outputFormat)
    {
        if ( query.isSelectType() )
            doSelectQuery(query, queryExecution, outputFormat) ;
        if ( query.isDescribeType() )
            doDescribeQuery(query, queryExecution, outputFormat) ;
        if ( query.isConstructType() )
            doConstructQuery(query, queryExecution, outputFormat) ;
        if ( query.isAskType() )
            doAskQuery(query, queryExecution, outputFormat) ;
        queryExecution.close() ;
    }

    public static void executeAlgebra(Op op, DatasetGraph dsg, ResultsFormat outputFormat)
    {
        QueryIterator qIter = Algebra.exec(op, dsg) ;

        List vars = null ;
        if ( op instanceof OpProject )
            vars = Var.varNames(((OpProject)op).getVars()) ;
        else
            vars = Var.varNames(OpVars.allVars(op)) ;

        ResultSet results = ResultSetFactory.create(qIter, vars) ;
        outputResultSet(results, null, outputFormat) ;
     }
    
    public static void outputResultSet(ResultSet results, Prologue prologue, ResultsFormat outputFormat)
    {
        boolean done = false ;
        if ( prologue == null )
            prologue = new Prologue(globalPrefixMap) ;

        if ( outputFormat.equals(ResultsFormat.FMT_UNKNOWN) )
            outputFormat = ResultsFormat.FMT_TEXT ;
        
        if ( outputFormat.equals(ResultsFormat.FMT_NONE) ||
             outputFormat.equals(ResultsFormat.FMT_COUNT) )
        {
            int count = ResultSetFormatter.consume(results) ;
            if ( outputFormat.equals(ResultsFormat.FMT_COUNT) )
            {
                System.out.println("Count = "+count) ;
            }
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_RDF) ||
             outputFormat.equals(ResultsFormat.FMT_RDF_N3) ||
             outputFormat.equals(ResultsFormat.FMT_RDF_TTL) )
        {
            Model m = ResultSetFormatter.toModel(results) ;
            m.setNsPrefixes(prologue.getPrefixMapping()) ;
            RDFWriter rdfw = m.getWriter("TURTLE") ;
            m.setNsPrefix("rs", ResultSetGraphVocab.getURI()) ;
            rdfw.write(m, System.out, null) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_XML) )
        {
            ResultSetFormatter.outputAsXML(System.out, results) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_JSON) )
        {
            ResultSetFormatter.outputAsJSON(System.out, results) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_SSE) )
        {
            ResultSetFormatter.outputAsSSE(System.out, results, prologue) ;
            done = true ;
        }
        
        if ( outputFormat.equals(ResultsFormat.FMT_TEXT) )
        {
            ResultSetFormatter.out(System.out, results, prologue) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_TUPLES) )
        {
            PlainFormat pFmt = new PlainFormat(System.out, prologue) ;
            ResultSetApply a = new ResultSetApply(results, pFmt) ;
            a.apply() ;
            done = true ;
        }

        if ( ! done )
            System.err.println("Unknown format request: "+outputFormat) ;
        results = null ;

        System.out.flush() ;
    }

    private static void doSelectQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ; 
        ResultSet results = qe.execSelect() ;
        outputResultSet(results, query, outputFormat) ;
    }


    private static void doDescribeQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_RDF_TTL ;

        Model r = qe.execDescribe() ;
        writeModel(query, r, outputFormat) ;
    }


    private static void doConstructQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_RDF_TTL ;

        Model r = qe.execConstruct() ;
        writeModel(query, r, outputFormat) ;
    }

    private static void writeModel(Query query, Model model, ResultsFormat outputFormat)
    {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ;

        if ( outputFormat.equals(ResultsFormat.FMT_NONE) )
            return ;

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT))
        {
            String qType = "" ;
            if ( query.isDescribeType() ) qType = "DESCRIBE" ;
            if ( query.isConstructType() ) qType = "CONSTRUCT" ;

            System.out.println("# ======== "+qType+" results ") ;
            model.write(System.out, "N3", null) ; // Base is meaningless
            System.out.println("# ======== ") ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_XML) )
        {
            model.write(System.out, "RDF/XML-ABBREV", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_TTL) )
        {
            model.write(System.out, "N3", null) ;
            return ;
        }
        
        if ( outputFormat.equals(ResultsFormat.FMT_RDF_N3) )
        {
            model.write(System.out, "N3", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_NT) )
        {
            model.write(System.out, "N_TRIPLES", null) ;
            return ;
        }

        System.err.println("Unknown format: "+outputFormat.getSymbol()) ;
    }

    private static void doAskQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        boolean b = qe.execAsk() ;

        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ;

        if ( outputFormat.equals(ResultsFormat.FMT_RS_XML) )
        {
            ResultSetFormatter.outputAsXML(System.out, b) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_N3) || 
        outputFormat.equals(ResultsFormat.FMT_RDF_TTL) )
        {
            ResultSetFormatter.outputAsRDF(System.out, "TURTLE", b) ;
            System.out.flush() ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_JSON) )
        {
            ResultSetFormatter.outputAsJSON(System.out, b) ;
            return ;
        }


        if ( outputFormat.equals(ResultsFormat.FMT_TEXT) )
        {
            //ResultSetFormatter.out(System.out, b) ;
            System.out.println("Ask => "+(b?"Yes":"No")) ;
            return ;
        }

        System.err.println("Unknown format: "+outputFormat.getSymbol()) ;
    }
    
    /** Execute a query, expecting the result to be one row, one column.  Return that one RDFNode */
    public static RDFNode getExactlyOne(String qs, Model model)
    { return getExactlyOne(qs, DatasetFactory.create(model)) ; }
    
    /** Execute a query, expecting the result to be one row, one column.  Return that one RDFNode */
    public static RDFNode getExactlyOne(String qs, Dataset ds)
    {
        Query q = QueryFactory.create(qs) ;
        if ( q.getResultVars().size() != 1 )
            throw new ARQException("getExactlyOne: Must have exactly one result columns") ;
        String varname = (String)q.getResultVars().get(0) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds);
        return getExactlyOne(qExec, varname) ;
    }
    
    /** Execute, expecting the result to be one row, one column.  Return that one RDFNode or throw an exception */
    public static RDFNode getExactlyOne(QueryExecution qExec, String varname)
    {
        try {
            ResultSet rs = qExec.execSelect() ;
            
            if ( ! rs.hasNext() )
                throw new ARQException("Not found: var ?"+varname) ;

            QuerySolution qs = rs.nextSolution() ;
            RDFNode r = qs.get(varname) ;
            if ( rs.hasNext() )
                throw new ARQException("More than one: var ?"+varname) ;
            return r ;
        } finally { qExec.close() ; }
    }
    
    /** Execute, expecting the result to be one row, one column. 
     * Return that one RDFNode or null
     * Throw excpetion if more than one.
     */
    public static RDFNode getOne(QueryExecution qExec, String varname)
    {
        try {
            ResultSet rs = qExec.execSelect() ;
            
            if ( ! rs.hasNext() )
                return null ;

            QuerySolution qs = rs.nextSolution() ;
            RDFNode r = qs.get(varname) ;
            if ( rs.hasNext() )
                throw new ARQException("More than one: var ?"+varname) ;
            return r ;
        } finally { qExec.close() ; }
    }
        

}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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