/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.algebra.op.*;

/** Apply a visitor to the whole structure of Ops, recursively.
 *  Visit sub Op before the current level
 */

public class OpWalker
{
    public static void walk(Op op, OpVisitor visitor)
    {
        op.visit(new WalkerVisitor(visitor, null, null)) ;
    }
    
    public static void walk(Op op, OpVisitor visitor, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        op.visit(new WalkerVisitor(visitor, beforeVisitor, afterVisitor)) ;
    }
    
    private static final class WalkerVisitor extends OpVisitorByType
    {
        private OpVisitor beforeVisitor = null ;
        private OpVisitor afterVisitor = null ;
        private OpVisitor visitor ;

        public WalkerVisitor(OpVisitor visitor, OpVisitor beforeVisitor, OpVisitor afterVisitor)
        { 
            this(visitor) ;
            this.beforeVisitor = beforeVisitor ;
            this.afterVisitor = afterVisitor ;
        }

        public WalkerVisitor(OpVisitor visitor) { this.visitor = visitor ; }
        
        private void before(Op op)
        { 
            if ( beforeVisitor != null )
                op.visit(beforeVisitor) ;
        }

        private void after(Op op)
        {
            if ( afterVisitor != null )
                op.visit(afterVisitor) ;
        }
        
        protected void visitN(OpN op)
        {
            before(op) ;
            for ( Iterator iter = op.iterator() ; iter.hasNext() ; )
            {
                Op sub = (Op)iter.next() ;
                sub.visit(this) ;
            }
            if ( visitor != null ) op.visit(visitor) ;
            after(op) ;
        }

        protected void visit2(Op2 op)
        {
            before(op) ;
            if ( op.getLeft() != null ) op.getLeft().visit(this) ;
            if ( op.getRight() != null ) op.getRight().visit(this) ;
            if ( visitor != null ) op.visit(visitor) ;      
            after(op) ; 
        }
        
        protected void visit1(Op1 op)
        {
            before(op) ;
            if ( op.getSubOp() != null ) op.getSubOp().visit(this) ;
            if ( visitor != null ) op.visit(visitor) ; 
            after(op) ;
        }
        
        protected void visit0(Op0 op)         
        {  
            before(op) ;
            if ( visitor != null ) op.visit(visitor) ;
            after(op) ;
        }
        
        protected void visitExt(OpExt op)
        {
            before(op) ;
            if ( visitor != null ) op.visit(visitor) ;
            after(op) ;
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