/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExprVars
{
    interface Action { void var(Collection acc, ExprVar nv) ; }
    
    public static Set getVarsMentioned(Expr expr)
    {
        Set acc = new HashSet() ;
        varsMentioned(acc, expr) ;
        return acc ;
    }
    
    public static void varsMentioned(Collection acc, Expr expr)
    {
        ExprVars.Action action =
            new ExprVars.Action(){
                public void var(Collection acc, ExprVar nv)
                {
                    acc.add(nv.asVar()) ;
                }
            } ;
        ExprVarsWorker vv = new ExprVarsWorker(acc, action) ;
        ExprWalker.walk(vv, expr) ;
    }
    
    public static Set getVarNamesMentioned(Expr expr)
    {
        Set acc = new HashSet() ;
        varNamesMentioned(acc, expr) ;
        return acc ;
    }
    
    public static void varNamesMentioned(Collection acc, Expr expr)
    {
        ExprVars.Action action =
            new ExprVars.Action(){
                public void var(Collection acc, ExprVar nv)
                {
                    acc.add(nv.getVarName()) ;
                }
            } ;
        ExprVarsWorker vv = new ExprVarsWorker(acc, action) ;
        ExprWalker.walk(vv, expr) ;
    }
    
    static class ExprVarsWorker extends ExprVisitorBase
    {
        Collection acc ;
        Action action ;
        
        public ExprVarsWorker(Collection acc, Action action)
        { this.acc = acc ; this.action = action ; }
        
        public void visit(ExprVar nv)
        { action.var(acc, nv) ; }
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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