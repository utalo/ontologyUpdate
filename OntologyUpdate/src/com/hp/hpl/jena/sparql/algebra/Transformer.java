/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.util.ALog;

public class Transformer
{
    static boolean noDupIfSame = true ;
    
    public static Op transform(Transform transform, Op op)
    { return transform(transform, op, null, null) ; }
    
    public static Op transform(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        if ( op == null )
        {
            ALog.warn(Transformer.class, "Attempt to transform a null Op - ignored") ;
            return op ;
        }

        TransformApply v = new TransformApply(transform) ;
        OpWalker.walk(op, v, beforeVisitor, afterVisitor) ;
        Op r = v.result() ;
        return r ;
        
    }

    private Transformer() { }
    
    private static final 
    class TransformApply extends OpVisitorByType
    {
        private Transform transform = null ;
        private Stack stack = new Stack() ;
        private Op pop() { return (Op)stack.pop(); }
        
        private void push(Op op)
        { 
            // Including nulls
            stack.push(op) ;
        }
        
        public TransformApply(Transform transform)
        { 
            this.transform = transform ;
        }
        
        public Op result()
        { 
            if ( stack.size() != 1 )
                ALog.warn(this, "Stack is not aligned") ;
            return pop() ; 
        }
    
        protected void visit0(Op0 op)
        {
            push(op.apply(transform)) ;
        }
        
        protected void visit1(Op1 op)
        {
            Op subOp = null ;
            if ( op.getSubOp() != null )
                subOp = pop() ;
            push(op.apply(transform, subOp)) ;
        }
    
        protected void visit2(Op2 op)
        { 
            Op left = null ;
            Op right = null ;
    
            // Must do right-left because the pushes onto the stack were left-right. 
            if ( op.getRight() != null )
                right = pop() ;
            if ( op.getLeft() != null )
                left = pop() ;
            Op opX = op.apply(transform, left, right) ; 
            push(opX) ;
        }
        
        protected void visitN(OpN op)
        {
            List x = new ArrayList(op.size()) ;     // Maybe be slightly too many.
            
            
            for ( Iterator iter = op.iterator() ; iter.hasNext() ; )
            {
                Op sub = (Op)iter.next() ;
                Op r = pop() ;
                // Skip nulls.
                if ( r != null )
                    // Add in reverse.
                    x.add(0, r) ;
            }
            Op opX = op.apply(transform, x) ;  
            push(opX) ;
        }
        
        protected void visitExt(OpExt op)
        {
            push(transform.transform(op)) ;
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