/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.Utils;

public class OpAssign extends Op1
{
    private VarExprList assignments ;
    
    static public Op assign(Op op, Var var, Expr expr)
    {
        // Compress assignments if possible.
        if ( ! ( op instanceof OpAssign ) )
            return createAssign(op, var, expr) ;
        
        OpAssign opAssign = (OpAssign)op ;
        if ( opAssign.assignments.contains(var) )
            // Must nest - variable already used.
            return createAssign(op, var, expr) ;

        // Merge assignment
        opAssign.assignments.add(var, expr) ;
        return opAssign ;
    }
    
    static private Op createAssign(Op op, Var var, Expr expr)
    {
        VarExprList x = new VarExprList() ;
        x.add(var, expr) ;
        return new OpAssign(op, x) ;
    }        
    
    public OpAssign(Op subOp)
    {
        super(subOp) ;
        assignments = new VarExprList() ;
    }
    
    public OpAssign(Op subOp, VarExprList exprs)
    {
        super(subOp) ;
        assignments = exprs ;
    }
    
    public String getName() { return Tags.tagAssign ; }
    
    public void add(Var var, Expr expr)
    { assignments.add(var, expr) ; }

    public VarExprList getVarExprList() { return assignments ; }

    public int hashCode()
    { return assignments.hashCode() ^ getSubOp().hashCode() ; }

    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    public Op copy(Op subOp)
    {
        OpAssign op = new OpAssign(subOp, new VarExprList(getVarExprList())) ;
        return op ;
    }

    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpAssign) )
            return false ;
        OpAssign assign = (OpAssign)other ;
        
        if ( ! Utils.equals(assignments, assign.assignments) )
            return false ;
        return getSubOp().equalTo(assign.getSubOp(), labelMap) ;
    }

    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }
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