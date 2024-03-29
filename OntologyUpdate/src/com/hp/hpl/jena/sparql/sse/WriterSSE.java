/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph;
import com.hp.hpl.jena.sparql.sse.writers.WriterNode;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class WriterSSE
{
    // No need for SerializationContext forms because these are the external intefraces
    // PrintStream [, Base] [, PrefixMap]
    
    public static void out(IndentedWriter out, Node node, Prologue prologue)
    { WriterNode.output(out, node, sCxt(prologue)) ; }
    
    public static void out(IndentedWriter out, Triple triple, Prologue prologue)
    { WriterNode.output(out, triple, sCxt(prologue)) ; }

    public static void out(IndentedWriter out, Expr expr, Prologue prologue)
    { WriterExpr.output(out, expr, sCxt(prologue)) ; }

    public static void out(IndentedWriter out, Op op, Prologue prologue)
    { WriterOp.output(out, op, sCxt(prologue)) ; }
    
    public static void out(IndentedWriter out, Graph g, Prologue prologue)
    { WriterGraph.output(out, g, sCxt(prologue)) ; }
    
    public static void out(IndentedWriter out, DatasetGraph dsg, Prologue prologue)
    { WriterGraph.output(out, dsg, sCxt(prologue)) ; }

    private static SerializationContext sCxt(Prologue prologue)
    { return new SerializationContext(prologue) ; }
    
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