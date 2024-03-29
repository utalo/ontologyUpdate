/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpPropFunc;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.sparql.util.graph.GNode;
import com.hp.hpl.jena.sparql.util.graph.GraphList;

public class PropertyFunctionGenerator
{
    public static Op buildPropertyFunctions(OpBGP opBGP, Context context)
    {
        if ( opBGP.getPattern().isEmpty() )
            return opBGP ;
        return compilePattern(opBGP.getPattern(), context) ;
    }
    
    private static Op compilePattern(BasicPattern pattern, Context context)
    {   
        // Split into triples and property functions.

        PropertyFunctionRegistry registry = chooseRegistry(context) ;
    
        // 1/ Find property functions.
        //    Property functions may involve other triples (for list arguments)
        //    (but leave the property function triple in-place as a marker)
        // 2/ Find arguments for property functions
        //    (but leave the property function triple in-place as a marker)
        // 3/ For remaining triples, put into basic graph patterns,
        //    and string together the procedure calls and BGPs.
        
        List propertyFunctionTriples = new ArrayList() ;    // Property functions seen
        BasicPattern triples = new BasicPattern(pattern) ;  // A copy of all triples (later, it is mutated)
        
        // Find the triples invoking property functions, and those not.
        findPropertyFunctions(context, pattern, registry, propertyFunctionTriples) ;
        
        if ( propertyFunctionTriples.size() == 0 )
            //No property functions.
            return new OpBGP(pattern) ;
        
        Map pfInvocations = new HashMap() ;  // Map triple => property function instance 
        // Removes triples of list arguments.  This mutates 'triples'
        findPropertyFunctionArgs(context, triples, propertyFunctionTriples, pfInvocations) ;
        
        // Now make the OpSequence structure.
        Op op = makeStages(triples, pfInvocations) ;
        return op ;
    }

    private static void findPropertyFunctions(Context context, 
                                              BasicPattern pattern,
                                              PropertyFunctionRegistry registry,
                                              List propertyFunctionTriples)
    {
        // Step 1 : find property functions (if any); collect triples.
        // Not list arg triples at this point.
        for ( Iterator iter = pattern.iterator() ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next() ;
            if ( isMagicProperty(registry, t) )
                propertyFunctionTriples.add(t) ;
        }
    }

    
    private static void findPropertyFunctionArgs(Context context, 
                                                 BasicPattern triples,
                                                 List propertyFunctionTriples,
                                                 Map pfInvocations)
    {
        // Step 2 : for each property function, remove associated triples in list arguments; 
        // Leave the propertyFunction triple itself.

        for ( Iterator iter = propertyFunctionTriples.iterator() ; iter.hasNext(); )
        {
            Triple pf = (Triple)iter.next();
            PropertyFunctionInstance pfi = magicProperty(context, pf, triples) ;
            pfInvocations.put(pf, pfi) ;
        }
    }
    
    private static class PropertyFunctionInstance
    {
        Node predicate ;
        PropFuncArg subjArgs ;
        PropFuncArg objArgs ;
        
         PropertyFunctionInstance(PropFuncArg sArgs, Node predicate, PropFuncArg oArgs)
        {
            this.subjArgs = sArgs ;
            this.predicate = predicate ;
            this.objArgs = oArgs ;
        }
        
        ExprList argList()
        {
            ExprList exprList = new ExprList() ;
            argList(exprList, subjArgs) ;
            argList(exprList, objArgs) ;
            return exprList ;
        }
        
        PropFuncArg getSubjectArgList()     { return subjArgs ; }
        PropFuncArg getObjectArgList()         { return objArgs ; }

        private static void argList(ExprList exprList, PropFuncArg pfArg)
        {
            if ( pfArg.isNode() )
            {
                Node n = pfArg.getArg() ;
                Expr expr = ExprUtils.nodeToExpr(n) ;
                exprList.add(expr) ;
                return ;
            }
            
            for ( Iterator iter = pfArg.getArgList().iterator() ; iter.hasNext() ; )
            {
                Node n = (Node)iter.next() ;
                Expr expr = ExprUtils.nodeToExpr(n) ;
                exprList.add(expr) ;
            }
        }
    }

    private static Op makeStages(BasicPattern triples, Map pfInvocations)
    {
        // Step 3 : Make the operation expression.
        //   For each property function, insert the implementation 
        //   For each block of non-property function triples, make a BGP.
        
        Op op = null; 
        BasicPattern pattern = null ;
        for ( Iterator iter = triples.iterator() ; iter.hasNext(); )
        {
            Triple t = (Triple)iter.next() ;
            
            if ( pfInvocations.containsKey(t) )
            {
                op = flush(pattern, op) ;
                pattern = null ;
                PropertyFunctionInstance pfi = (PropertyFunctionInstance)pfInvocations.get(t) ;
                OpPropFunc opPF =  new OpPropFunc(t.getPredicate(), pfi.getSubjectArgList(), pfi.getObjectArgList(), op) ;
                op = opPF ;
                continue ;
            }       
                
            // Regular triples - make sure there is a basic pattern in progress. 
            if ( pattern == null )
                pattern = new BasicPattern() ;
            pattern.add(t) ;
        }
        op = flush(pattern, op) ;
        return op ;
    }
    
    private static Op flush(BasicPattern pattern, Op op)
    {
        if ( pattern == null || pattern.isEmpty() )
        {
            if ( op == null )
                return OpTable.unit() ;
            return op ;
        }
        OpBGP opBGP = new OpBGP(pattern) ;
        return OpSequence.create(op, opBGP) ;
    }

    public static PropertyFunctionRegistry chooseRegistry(Context context)
    {
        PropertyFunctionRegistry registry = PropertyFunctionRegistry.get(context) ;
        // Else global
        if ( registry == null )
            registry = PropertyFunctionRegistry.get() ;
        return registry ;
    }
    
    private static boolean isMagicProperty(PropertyFunctionRegistry registry, Triple pfTriple)
    {
        if ( ! pfTriple.getPredicate().isURI() ) 
            return false ;

        if ( registry.manages(pfTriple.getPredicate().getURI()) )
            return true ;
        
        return false ;
    }
    
    // Remove all triples associated with this magic property.
    // Make an instance record.
   private static PropertyFunctionInstance magicProperty(Context context,
                                                         Triple pfTriple,
                                                         BasicPattern triples)
    {
        List listTriples = new ArrayList() ;

        GNode sGNode = new GNode(triples, pfTriple.getSubject()) ;
        GNode oGNode = new GNode(triples, pfTriple.getObject()) ;
        List sList = null ;
        List oList = null ;
        
        if ( GraphList.isListNode(sGNode) )
        {
            sList = GraphList.members(sGNode) ;
            GraphList.allTriples(sGNode, listTriples) ;
        }
        if ( GraphList.isListNode(oGNode) )
        {
            oList = GraphList.members(oGNode) ;
            GraphList.allTriples(oGNode, listTriples) ;
        }
        
        PropFuncArg subjArgs = new PropFuncArg(sList, pfTriple.getSubject()) ;
        PropFuncArg objArgs =  new PropFuncArg(oList, pfTriple.getObject()) ;
        
        // Confuses single arg with a list of one. 
        PropertyFunctionInstance pfi = new PropertyFunctionInstance(subjArgs, pfTriple.getPredicate(), objArgs) ;
        
        triples.getList().removeAll(listTriples) ;
        return pfi ;
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