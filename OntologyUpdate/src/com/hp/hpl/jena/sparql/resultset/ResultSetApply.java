/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import java.util.* ;

/** A class to walk a result set.
 * 
 * @author Andy Seaborne
 */

public class ResultSetApply
{
    ResultSetProcessor proc = null ;
    ResultSet rs = null ;
    
    public ResultSetApply(ResultSet rs, ResultSetProcessor proc)
    {
        this.proc = proc ;
        this.rs = rs ;
    }
    
    public void apply()
    {
        proc.start(rs) ;
        for ( ; rs.hasNext() ; )
        {
            QuerySolution qs = (QuerySolution)rs.next() ;
            proc.start(qs) ;
            for ( Iterator iter = rs.getResultVars().iterator() ; iter.hasNext() ; )
            {
                String varName = (String)iter.next() ;
                RDFNode node = qs.get(varName) ;
                // node may be null
                proc.binding(varName, node) ;
            }
            proc.finish(qs) ;
        }
        proc.finish(rs) ;
    }
    
    public static void apply(ResultSet rs, ResultSetProcessor proc)
    {
        ResultSetApply rsa = new ResultSetApply(rs, proc) ;
        rsa.apply() ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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