/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.QuerySolutionBase;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.FmtUtils;

/** Implementation of QuerySolution that is backed by an in-memory map.
 * 
 * @author Andy Seaborne
 */ 

public class QuerySolutionMap extends QuerySolutionBase
{
    private Map map = new HashMap() ;
    
    public QuerySolutionMap() { } 

    public void add(String name, RDFNode node)
    { map.put(Var.canonical(name), node) ; }
    
    protected RDFNode _get(String varName)          { return (RDFNode)map.get(Var.canonical(varName)) ; } 

    protected boolean _contains(String varName)     { return map.containsKey(Var.canonical(varName)) ; } 

    public Iterator varNames()                   { return map.keySet().iterator() ; }
    
    public String toString()
    {
        String tmp = "" ;
        String sep = "" ;
        for ( Iterator iter = varNames() ; iter.hasNext() ; )
        {
            String varName = (String)iter.next() ;
            RDFNode n = _get(varName) ;
            String nStr = FmtUtils.stringForRDFNode(n) ;
            tmp = tmp+sep+"( ?"+varName+", "+nStr+" )" ;
        }
        return tmp ;
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