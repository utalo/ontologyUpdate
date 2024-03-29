/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase;

public abstract class PlanBase extends PrintSerializableBase implements Plan
{
    // Merge with PlanOp?
    // Enforces "use once"
    private Op op = null ;
    protected Closeable closeable = null ;
    protected boolean closed = false ;
    private boolean iteratorProduced = false ;
    
    protected abstract QueryIterator iteratorOnce() ;

    protected PlanBase(Op op, Closeable closeable)  { this.op = op ; this.closeable = closeable ; } 
    
    public Op getOp()       { return op ; }
    
    final
    public QueryIterator iterator() 
    {
        if ( iteratorProduced )
        {
            throw new ARQInternalErrorException("Attempt to use the iterator twice") ;
        }
        iteratorProduced = true ;
        return iteratorOnce() ;
    }

    public void output(IndentedWriter out)
    {
        SerializationContext sCxt = new SerializationContext(ARQConstants.getGlobalPrefixMap()) ;
        output(out, sCxt) ;
    }
    
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        op.output(out, sCxt) ;
    }
    
    public void close()
    { 
        if ( closed )
            return ;
        if ( closeable != null )
            // called once
            // Two routes - explicit QueryExecution.close
            // or natural end of QueryIterator (see PlanOp)
            closeable.close() ;
        closed = true ;
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