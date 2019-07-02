/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

/* Generated By:JJTree: Do not edit this line. Q_Var.java */

package com.hp.hpl.jena.sparql.lang.rdql;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.IndexValues;
import com.hp.hpl.jena.graph.query.Valuator;
import com.hp.hpl.jena.graph.query.VariableIndexes;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class Q_Var extends ExprNode implements ExprRDQL
{
    String varName ;
    int index ;
    //VariableIndexes varIndexes ;

    public Q_Var(int id) { super(id); }
    public Q_Var(RDQLParser p, int id) { super(p, id); }

    public void jjtClose()
    {
    }

    public void setName(String vn)
    { 
        if ( vn.startsWith("?") )
            vn = vn.substring(1) ;
        varName = vn ;
    }

    public Var asVar() { return Var.alloc(varName) ; }
    
    public RDQL_NodeValue evalRDQL(com.hp.hpl.jena.query.Query q, IndexValues env)
    {
        // Result is a copy as a bound variable.
        com.hp.hpl.jena.graph.Node v = (Node)env.get(index) ;
        if ( v == null )
        {
            ALog.warn(this, "Unbound variable: "+varName) ;
            WorkingVar tmp = new WorkingVar() ;
            tmp.setString("unset: "+varName+"/"+index) ;
            return tmp ;
        }
        WorkingVar var = new WorkingVar() ;
        var.setNode(v) ;
        return var ;
    }

    public String toString() { return "?"+varName ; }

    public String asInfixString() { return toString() ; }
    public String asPrefixString() { return toString() ; }

    public void format(IndentedWriter w)
    {
        w.print(this.asPrefixString()) ;
    }

    // graph.query.Expression
    public Valuator prepare(VariableIndexes vi)
    {
        //super.prepare(vi) ; // Not needed - we work down, not up.
        //varIndexes = vi ;  // Just need to remember our index
        index = vi.indexOf(varName) ;
        return this ;
    }
    
    public boolean isVariable()      { return true ; }
    public String getName()          { return varName ; } // For variables only


}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
