/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

public class AggCountVar implements AggregateFactory
{
    // ---- COUNT(?var)
    
    // ---- AggregatorFactory
    private Var var ;

    public AggCountVar(Var var) { this.var = var ; } 

    public Aggregator create()
    {
        // One per each time there is an aggregation.
        // For count(*) - one per group operator (so shared with having clause)
        return new AggCountVarWorker() ;
    }
    
    // ---- Aggregator
    private class AggCountVarWorker extends AggregatorBase
    {
        public AggCountVarWorker()
        {
            super() ;
        }

        public String toString() { return "count("+var+")" ; }
        public String toPrefixString() { return "(count "+var+")" ; }

        protected Accumulator createAccumulator()
        { 
            return new AccCountVar() ;
        }
        
        public Node getValueEmpty()     { return NodeValue.nodeIntZERO ; } 
    }

    // ---- Accumulator
    private class AccCountVar implements Accumulator
    {
        private long count = 0 ;
        public AccCountVar()   { }
        public void accumulate(Binding binding, FunctionEnv functionEnv)
        { 
            if ( binding.contains(var) )
                count++ ;
        }
        public NodeValue getValue()             { return NodeValue.makeInteger(count) ; }
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