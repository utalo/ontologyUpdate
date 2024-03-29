/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.Tags;

public class BuilderResultSet
{
       
    public static ResultSet build(Item item)
    {
        BuilderLib.checkTagged(item, Tags.tagResultSet, "Not a (resultset ...)") ;
        
        //SPARQLResult result = new SPARQLResult() ;
        /*
         * (resultset (vars) ROWS) <-----
         * or 
         * (resultset (vars) ROWS)
         */
        
        ItemList list = item.getList() ; 
        
        List vars = BuilderNode.buildVarList(list.get(1)) ;
        // skip tag, skip vars.
        int start = 2 ;

        List bindings = new ArrayList() ;
        for ( int i = start ; i < list.size() ; i++ )
        {
            Item itemRow = list.get(i) ;
            Binding b = BuilderBinding.build(itemRow) ;
            bindings.add(b) ;
//            for ( Iterator iter = b.vars() ; iter.hasNext() ; )
//            { }
        }
        
        QueryIterator qIter = new QueryIterPlainWrapper(bindings.listIterator()) ;
        return new ResultSetStream(Var.varNames(vars), null, qIter) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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