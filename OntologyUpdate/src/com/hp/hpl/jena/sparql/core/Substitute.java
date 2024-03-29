/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.path.PathLib;

public class Substitute
{
    public static Op substitute(Op op, Binding b)
    {
        // Want to avoid cost if the binding is empty 
        // but the empty test is not zero-cost on non-empty things.
     
        if ( b instanceof BindingRoot )
            // The root binding is empty.
            return op ;
        
        return Transformer.transform(new OpSubstituteWorker(b), op) ;
    }
    
    public static Op substitute(Op op, Var var, Node node)
    {
        Binding b = new Binding1(null, var, node) ;
        return Transformer.transform(new OpSubstituteWorker(b), op) ;
    }
    
    public static BasicPattern substitute(BasicPattern bgp, Binding binding)
    {
        if ( binding instanceof BindingRoot )
            // The root binding is empty.
            return bgp ;
        
        BasicPattern bgp2 = new BasicPattern() ;
        for ( Iterator iter = bgp.iterator() ; iter.hasNext() ; )
        {
            Triple triple = (Triple)iter.next() ;
            Triple t = substitute(triple, binding) ;
            bgp2.add(t) ;
        }
        return bgp2 ;
    }
    
    public static Triple substitute(Triple triple, Binding binding)
    {
        Node s = substitute(triple.getSubject(), binding) ;
        Node p = substitute(triple.getPredicate(), binding) ;
        Node o = substitute(triple.getObject(), binding) ;
        Triple t = new Triple(s, p, o) ;
        return t ;
    }

    public static Node substitute(Node n, Binding b)
    {
        return Var.lookup(b, n) ;
    }

    // ----
    private static class OpSubstituteWorker extends TransformCopy
    {
        private Binding binding ;

        public OpSubstituteWorker(Binding binding) 
        {
            super(TransformCopy.COPY_ALWAYS) ;
            this.binding = binding ;
        }

        //@Override
        public Op transform(OpBGP opBGP)
        {
            BasicPattern bgp = opBGP.getPattern() ;
            bgp = substitute(bgp, binding) ;
            return new OpBGP(bgp) ;
        }

        //@Override
        public Op transform(OpQuadPattern quadPattern)
        {
            Node gNode = quadPattern.getGraphNode() ;
            Node g = substitute(gNode, binding) ;

            BasicPattern triples = new BasicPattern() ;
            for ( Iterator iter = quadPattern.getQuads().iterator() ; iter.hasNext() ; )
            {
                Quad quad = (Quad)iter.next() ;
                if ( ! quad.getGraph().equals(gNode) )
                    throw new ARQInternalErrorException("Internal error: quads block is not uniform over the graph node") ;
                Node s = substitute(quad.getSubject(), binding) ;
                Node p = substitute(quad.getPredicate(), binding) ;
                Node o = substitute(quad.getObject(), binding) ;
                Triple t = new Triple(s, p, o) ;
                triples.add(t) ;
            }

            return new OpQuadPattern(g, triples) ;
        }

        public Op transform(OpPath opPath)
        {
            return new OpPath(PathLib.substitute(opPath.getTriplePath(), binding)) ;
        }

        //@Override
        public Op transform(OpFilter filter, Op op)
        {
            ExprList exprs = filter.getExprs().copySubstitute(binding, true) ;
            return OpFilter.filter(exprs, op) ; 
        }

        public Op transform(OpGraph op, Op sub)
        {
            Node n = substitute(op.getNode(), binding) ;
            return new OpGraph(n, sub) ;
        }

        public Op transform(OpService op, Op sub)
        {
            Node n = substitute(op.getService(), binding) ;
            return new OpService(n, sub) ;
        }
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