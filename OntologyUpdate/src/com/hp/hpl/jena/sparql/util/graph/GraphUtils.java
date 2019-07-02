/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util.graph;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.util.NotUniqueException;
import com.hp.hpl.jena.sparql.util.PlainGraphMem;
import com.hp.hpl.jena.sparql.util.PropertyRequiredException;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.sparql.util.RefBoolean;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.sparql.util.TypeNotUniqueException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/** Graph utilities. 
 * @author Andy Seaborne
 */ 

public class GraphUtils
{
    private static RefBoolean usePlainGraph = new RefBoolean(ARQ.strictGraph) ;

    // These functions control creating models for datasets.
    
    // ---- Model
    
    public static Model makeDefaultModel()
    {
        return ModelFactory.createModelForGraph(makeDefaultGraph()) ;
    }

    public static Model makePlainModel()
    {
        return ModelFactory.createModelForGraph(makePlainGraph()) ;
    }

    
    public static Model makeJenaDefaultModel() { return ModelFactory.createDefaultModel() ; }
    
    // ---- Graph
    
    public static Graph makeDefaultGraph()
    { return usePlainGraph.getValue() ? makePlainGraph() : makeJenaDefaultGraph() ; }

    public static Graph makeJenaDefaultGraph() { return Factory.createDefaultGraph() ; }
    
    public static Graph makePlainGraph() { return new PlainGraphMem() ; } 
    
    // ------- List utilities
    
    public static List multiValueString(Resource r, Property p)
    {
        List nodes = multiValue(r, p) ;
        List values = new ArrayList() ;
    
        for ( Iterator iter= nodes.iterator() ; iter.hasNext() ; )
        {
            RDFNode n = (RDFNode)iter.next();
            if ( n.isLiteral() )
                values.add(((Literal)n).getString()) ;
        }
        return values ;
    }

    public static List multiValue(Resource r, Property p)
    {
        List values = new ArrayList() ;
        StmtIterator sIter = r.listProperties(p) ;
        while(sIter.hasNext())
        {
            Statement s = sIter.nextStatement() ;
            values.add(s.getObject()) ;
        }
        return values;
    }
    
    public static List multiValueResource(Resource r, Property p)
    {
        List nodes = multiValue(r, p) ;
        List values = new ArrayList() ;
    
        for ( Iterator iter= nodes.iterator() ; iter.hasNext() ; )
        {
            RDFNode n = (RDFNode)iter.next();
            if ( n.isResource() )
                values.add(n) ;
        }
        return values ;
    }

    public static List multiValueURI(Resource r, Property p)
    {
        List nodes = multiValue(r, p) ;
        List values = new ArrayList() ;
    
        for ( Iterator iter= nodes.iterator() ; iter.hasNext() ; )
        {
            RDFNode n = (RDFNode)iter.next();
            if ( n.isURIResource() )
                values.add(((Resource)n).getURI()) ;
        }
        return values ;
    }

    public static boolean exactlyOneProperty(Resource r, Property p)
    {
        StmtIterator sIter = r.listProperties(p) ;
        try {
            if ( ! sIter.hasNext() )
                throw new PropertyRequiredException(r, p) ;
            sIter.next() ;
            if ( sIter.hasNext() )
                throw new NotUniqueException(r, p) ;
        } finally { sIter.close() ; } 
        return true ;
    }

    public static boolean atmostOneProperty(Resource r, Property p)
    {
        StmtIterator sIter = r.listProperties(p) ;
        try {
            if ( ! sIter.hasNext() ) return true ;
            sIter.next() ;
            if ( sIter.hasNext() )
                throw new NotUniqueException(r, p) ;
        } finally { sIter.close() ; } 
        return true ;
    }

    public static String getStringValue(Resource r, Property p)
    {
        if ( ! atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        return s.getString() ; 
    }

    public static String getAsStringValue(Resource r, Property p)
    {
        if ( ! atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        if ( s.getObject().isResource() )
            return s.getResource().getURI() ;
        return s.getString() ; 
    }

    public static Resource getResourceValue(Resource r, Property p)
    {
        if ( ! atmostOneProperty(r, p) )
            throw new NotUniqueException(r, p) ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        return s.getResource() ;
    }

    public static Resource getResourceByType(Model model, Resource type)
    {
        // See also 
        StmtIterator sIter = model.listStatements(null, RDF.type, type) ;
        if ( ! sIter.hasNext() )
            return null ;
        Resource r = sIter.nextStatement().getSubject() ;
        if ( sIter.hasNext() )
            throw new TypeNotUniqueException(r) ;
        return r ;
    }
    
    public static Resource findRootByType(Model model, Resource atype)
    {
        String s = StringUtils.join("\n", new String[]{
            "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" ,
            "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT DISTINCT ?root { { ?root rdf:type ?ATYPE } UNION { ?root rdf:type ?t . ?t rdfs:subClassOf ?ATYPE } }"
        }) ;
        Query q = QueryFactory.create(s) ;
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        qsm.add("ATYPE", atype) ;

        QueryExecution qExec = QueryExecutionFactory.create(q, model, qsm);
        Resource r = (Resource)QueryExecUtils.getOne(qExec, "root") ;
        return r;
    }
    
    
    public static String fmtURI(Resource r)
    { return r.getModel().shortForm(r.getURI()) ;  }
    
    /** All subjects and objects, no duplicates. */
    public static Iterator allNodes(Graph graph)
    {
        Set x = new HashSet(1000) ;
        ExtendedIterator iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next();
            x.add(t.getSubject()) ;
            x.add(t.getObject()) ;
        }
        iter.close() ;
        return x.iterator() ;
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