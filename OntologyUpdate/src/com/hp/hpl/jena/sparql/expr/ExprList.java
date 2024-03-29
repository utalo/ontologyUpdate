/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.util.Context;

public class ExprList
{
    private List expressions = new ArrayList() ;
    
    public ExprList() {}
    public ExprList(ExprList other) { expressions.addAll(other.expressions) ; }
    public ExprList(Expr expr)
    {
        this() ;
        expressions.add(expr) ;
    }
    
    public boolean isSatisfied(Binding binding, ExecutionContext execCxt)
    {
        // Dream of generics
        for ( Iterator iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next();
            if ( ! expr.isSatisfied(binding, execCxt) )
                return false ;
        }
        return true ;
    }
    
    public Expr get(int idx) { return (Expr)expressions.get(idx) ; }
    public int size() { return expressions.size() ; }
    public boolean isEmpty() { return expressions.isEmpty() ; }
    
    public Set getVarsMentioned()
    {
        Set x = new HashSet() ;
        varsMentioned(x) ;
        return x ;
    }
    
    public void varsMentioned(Collection acc)
    {
        for ( Iterator iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next();
            expr.varsMentioned(acc) ;
        }
    }
    
    public ExprList copySubstitute(Binding binding) { return copySubstitute(binding, true) ; }
    public ExprList copySubstitute(Binding binding, boolean foldConstants)
    {
        ExprList x = new ExprList() ;
        for ( Iterator iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next();
            expr = expr.copySubstitute(binding, foldConstants) ;
            x.add(expr) ;
        }
        return x ;
    }
    public void addAll(ExprList exprs) { expressions.addAll(exprs.getList()) ; }
    public void add(Expr expr) { expressions.add(expr) ; }
    public List getList() { return expressions ; }
    public Iterator iterator() { return expressions.iterator() ; }
    
    public void prepareExprs(Context context)
    {
        // Give each expression the chance to set up (bind functions)
        for ( Iterator iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next() ;
            ExprWalker.walk(new ExprBuild(context), expr) ;
        }
    }
    
    public String toString()
    { return expressions.toString() ; }
    
    public int hashCode() { return expressions.hashCode() ; }

    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof ExprList ) ) return false ;
        ExprList exprs = (ExprList)other ;
        return expressions.equals(exprs.expressions) ;
    }
    public static ExprList splitConjunction(ExprList exprList1)
    {
        ExprList exprList2 = new ExprList() ;
        for ( Iterator iter = exprList1.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next() ;
            split(exprList2, expr) ;
        }
        return exprList2 ;
    }
    
    private static ExprList splitConjunction(Expr expr)
    {
        ExprList exprList = new ExprList() ;
        split(exprList, expr) ;
        return exprList ;
    }
    
    
    private static void split(ExprList exprList, Expr expr)
    {
        // Explode &&-chain to exprlist.
        while ( expr instanceof E_LogicalAnd )
        {
            E_LogicalAnd x = (E_LogicalAnd)expr ;
            Expr left = x.getArg1() ;
            Expr right = x.getArg2() ;
            split(exprList, left) ;
            expr = right ;
        }
        // Drop through and add remaining
        exprList.add(expr) ;
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