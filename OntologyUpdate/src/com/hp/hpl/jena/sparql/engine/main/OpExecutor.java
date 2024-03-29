/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.*;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterGraph;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterJoin;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterLeftJoin;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterOptionalIndex;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterService;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterUnion;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.procedure.ProcEval;
import com.hp.hpl.jena.sparql.procedure.Procedure;

/**
 * Turn an Op expression into an execution of QueryIterators.
 * 
 * Does not consider optimizing the algebra expression (that should happen
 * elsewhere). BGPs are still subject to StageBuilding during iterator
 * execution. During execution, when a substitution into an algebra expression
 * happens (in other words, a streaming operation, index-join-like), there is a
 * call into the executor each time so it does not just happen once before a
 * query starts.
 * 
 * @author Andy Seaborne
 */

public class OpExecutor
{
    // Set this to a different factory implementation to have a different OpExecutor.  
    protected static final OpExecutorFactory stdFactory = new OpExecutorFactory(){
        public OpExecutor create(ExecutionContext execCxt)
        {
            return new OpExecutor(execCxt) ;
        }} ;  
//    public static Factory factory = stdFactory ; 

    
    private static OpExecutor createOpExecutor(ExecutionContext execCxt)
    {
        OpExecutorFactory factory = execCxt.getExecutor() ;
        if ( factory == null )
            factory = stdFactory ;
        if ( factory == null )
            return new OpExecutor(execCxt) ; 
        return factory.create(execCxt) ;
    }
    
    // -------
    
    static QueryIterator execute(Op op, ExecutionContext execCxt)
    {
        return execute(op, createRootQueryIterator(execCxt), execCxt) ;
    }
    
    // Public interface is via QC.execute.
    static QueryIterator execute(Op op, QueryIterator qIter, ExecutionContext execCxt)
    {
        OpExecutor exec = createOpExecutor(execCxt) ;
        QueryIterator q = exec.executeOp(op, qIter) ;
        return q ;
    }

    // -------- The object starts here --------
    
    protected ExecutionContext execCxt ;
    protected ExecutionDispatch dispatcher = null ;

    protected OpExecutor(ExecutionContext execCxt)
    { 
        this.execCxt = execCxt ;
        dispatcher = new ExecutionDispatch(this) ;
    }

//    public QueryIterator executeOp(Op op)
//    {
//        return executeOp(op, root()) ;
//    }

    // ---- The recursive step.
    
    public QueryIterator executeOp(Op op, QueryIterator input)
    {
        return dispatcher.exec(op, input) ;
    }
    
    // ---- All the cases
    
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input)
    {
        BasicPattern pattern = opBGP.getPattern() ;
        return StageBuilder.execute(pattern, input, execCxt) ;
    }

    protected QueryIterator execute(OpTriple opTriple, QueryIterator input)
    {
        return execute(opTriple.asBGP(), input) ;
    }

    protected QueryIterator execute(OpQuadPattern quadPattern, QueryIterator input)
    {
        if ( false )
        {
            if ( quadPattern.isDefaultGraph() )
            {
                // Easy case.
                OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern()) ;
                return execute(opBGP, input) ;  
            }
        }        
        // Turn into a OpGraph/OpBGP.
        throw new ARQNotImplemented("execute/OpQuadPattern") ;
    }

    protected QueryIterator execute(OpPath opPath, QueryIterator input)
    {
        return new QueryIterPath(opPath.getTriplePath(), input, execCxt) ;
    }

    protected QueryIterator execute(OpProcedure opProc, QueryIterator input)
    {
        Procedure procedure = ProcEval.build(opProc, execCxt) ;
        QueryIterator qIter = executeOp(opProc.getSubOp(), input) ;
        // Delay until query starts executing.
        return new QueryIterProcedure(qIter, procedure, execCxt) ;
    }

    protected QueryIterator execute(OpPropFunc opPropFunc, QueryIterator input)
    {
        Procedure procedure = ProcEval.build(opPropFunc.getProperty(), opPropFunc.getSubjectArgs(),opPropFunc.getObjectArgs(), execCxt) ;
        QueryIterator qIter = executeOp(opPropFunc.getSubOp(), input) ;
        return new QueryIterProcedure(qIter, procedure, execCxt) ;
    }


    protected QueryIterator execute(OpJoin opJoin, QueryIterator input)
    {
        // Look one level in for any filters with out-of-scope variables.
        boolean canDoLinear = JoinClassifier.isLinear(opJoin) ;

        if ( canDoLinear )
            // Streamed evaluation
            return stream(opJoin.getLeft(), opJoin.getRight(), input) ;
        
        // Can't do purely indexed (e.g. a filter referencing a variable out of scope is in the way)
        // To consider: partial substitution for improved performance (but does it occur for real?)
        
        QueryIterator left = executeOp(opJoin.getLeft(), input) ;
        QueryIterator right = executeOp(opJoin.getRight(), root()) ;
        QueryIterator qIter = new QueryIterJoin(left, right, execCxt) ;
        return qIter ;
        // Worth doing anything about join(join(..))?
    }

    // Pass iterator from left directly into the right.
    protected QueryIterator stream(Op opLeft, Op opRight, QueryIterator input)
    {
        QueryIterator left = executeOp(opLeft, input) ;
        QueryIterator right = executeOp(opRight, left) ;
        return right ;
    }

    // Pass iterator from one step directly into the next.
    protected QueryIterator execute(OpSequence opSequence, QueryIterator input)
    {
        QueryIterator qIter = input ;
        
        for ( Iterator iter = opSequence.iterator() ; iter.hasNext() ; )
        {
            Op sub = (Op)iter.next() ;
            qIter = executeOp(sub, qIter) ;
        }
        
        return qIter ;
    }
    
    protected QueryIterator execute(OpLeftJoin opLeftJoin, QueryIterator input)
    {
        ExprList exprs = opLeftJoin.getExprs() ;
        if ( exprs != null )
            exprs.prepareExprs(execCxt.getContext()) ;

        // Do an indexed substitute into the right if possible.
        boolean canDoLinear = LeftJoinClassifier.isLinear(opLeftJoin) ;
        
        if ( canDoLinear )
        {
            // Pass left into right for substitution before right side evaluation.
            // In an indexed left join, the LHS bindings are visible to the
            // RHS execution so the expression is evaluated by moving it to be 
            // a filter over the RHS pattern. 
            
            Op opLeft = opLeftJoin.getLeft() ;
            Op opRight = opLeftJoin.getRight() ;
            if (exprs != null )
                opRight = OpFilter.filter(exprs, opRight) ;
            QueryIterator left = executeOp(opLeft, input) ;
            QueryIterator qIter = new QueryIterOptionalIndex(left, opRight, execCxt) ;
            return qIter ;
        }

		// Not index-able.
        // Do it by sub-evaluation of left and right then left join.
        // Can be expensive if RHS returns a lot.
        // To consider: partial substitution for improved performance (but does it occur for real?)

        QueryIterator left = executeOp(opLeftJoin.getLeft(), input) ;
        QueryIterator right = executeOp(opLeftJoin.getRight(), root()) ;
        QueryIterator qIter = new QueryIterLeftJoin(left, right, exprs, execCxt) ;
        return qIter ;
    }

    protected QueryIterator execute(OpConditional opCondition, QueryIterator input)
    {
        if ( true )
            throw new ARQNotImplemented("OpCompile: OpConditional") ;
        //QueryIterOptionalIndex
        return null ;
    }
    
    protected QueryIterator execute(OpDiff opDiff, QueryIterator input)
    { 
        QueryIterator left = executeOp(opDiff.getLeft(), input) ;
        QueryIterator right = executeOp(opDiff.getRight(), root()) ;
        return new QueryIterDiff(left, right, execCxt) ;
    }
    
    protected QueryIterator execute(OpUnion opUnion, QueryIterator input)
    {
        List x = flattenUnion(opUnion) ;
        QueryIterator cIter = new QueryIterUnion(input, x, execCxt) ;
        return cIter ;
    }
    
    // Based on code from Olaf Hartig.
    protected List flattenUnion(OpUnion opUnion)
    {
        List x = new ArrayList() ;
        flattenUnion(x, opUnion) ;
        return x ;
    }
    
    protected void flattenUnion(List acc, OpUnion opUnion)
    {
        if (opUnion.getLeft() instanceof OpUnion)
            flattenUnion(acc, (OpUnion)opUnion.getLeft()) ;
        else
            acc.add( opUnion.getLeft() ) ;

        if (opUnion.getRight() instanceof OpUnion)
            flattenUnion(acc, (OpUnion)opUnion.getRight()) ;
        else
            acc.add( opUnion.getRight() ) ;
    }
    
    protected QueryIterator execute(OpFilter opFilter, QueryIterator input)
    {
        ExprList exprs = opFilter.getExprs() ;
        exprs.prepareExprs(execCxt.getContext()) ;
        
        Op base = opFilter.getSubOp() ;
        QueryIterator qIter = executeOp(base, input) ;

        for ( Iterator iter = exprs.iterator() ; iter.hasNext(); )
        {
            Expr expr = (Expr)iter.next() ;
            qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        }
        return qIter ;
    }

    protected QueryIterator execute(OpGraph opGraph, QueryIterator input)
    { 
        return new QueryIterGraph(input, opGraph, execCxt) ;
    }
    
    protected QueryIterator execute(OpService opService, QueryIterator input)
    {
        return new QueryIterService(input, opService, execCxt) ;
    }
    
    protected QueryIterator execute(OpDatasetNames dsNames, QueryIterator input)
    { 
        if ( true ) throw new ARQNotImplemented("OpDatasetNames") ;
        
        // Augment (join) iterator with a table.
        Table t = null ;
        Op left = null ; 
        Op right = OpTable.create(t) ;
        Op opJoin = OpJoin.create(left, right) ;
        return executeOp(opJoin , input) ;    //??
    }

    protected QueryIterator execute(OpTable opTable, QueryIterator input)
    { 
//        if ( input instanceof QueryIteratorBase )
//        {
//            String x = ((QueryIteratorBase)input).debug();
//            System.out.println(x) ;
//        }
//        
        if ( opTable.isJoinIdentity() )
            return input ;
        if ( input instanceof QueryIterRoot )
        {
            input.close() ;
            return opTable.getTable().iterator(execCxt) ;
        }
        //throw new ARQNotImplemented("Not identity table") ;
        QueryIterator qIterT = opTable.getTable().iterator(execCxt) ;
        //QueryIterator qIterT = root() ;
        QueryIterator qIter = new QueryIterJoin(input, qIterT, execCxt) ;
        return qIter ;
    }

    protected QueryIterator execute(OpExt opExt, QueryIterator input)
    { 
        try {
            QueryIterator qIter = opExt.eval(input, execCxt) ;
            if ( qIter != null )
                return qIter ;
        } catch (UnsupportedOperationException ex) { }
        // null or UnsupportedOperationException
        throw new QueryExecException("Encountered unsupported OpExt: "+opExt.getName()) ;
    }

    protected QueryIterator execute(OpLabel opLabel, QueryIterator input)
    {
      if ( ! opLabel.hasSubOp() )
          return input ;

      return executeOp(opLabel.getSubOp(), input) ;
    }

    protected QueryIterator execute(OpNull opNull, QueryIterator input)
    {
        // Loose the input.
        input.close() ;
        return new QueryIterNullIterator(execCxt) ;
    }

    protected QueryIterator execute(OpList opList, QueryIterator input)
    {
        return executeOp(opList.getSubOp(), input) ;
    }
    
    protected QueryIterator execute(OpOrder opOrder, QueryIterator input)
    { 
        QueryIterator qIter = executeOp(opOrder.getSubOp(), input) ;
        qIter = new QueryIterSort(qIter, opOrder.getConditions(), execCxt) ;
        return qIter ;
    }

    protected QueryIterator execute(OpProject opProject, QueryIterator input)
    {
        QueryIterator  qIter = executeOp(opProject.getSubOp(), input) ;
        qIter = new QueryIterProject(qIter, opProject.getVars(), execCxt) ;
        return qIter ;
    }

    protected QueryIterator execute(OpSlice opSlice, QueryIterator input)
    { 
        QueryIterator qIter = executeOp(opSlice.getSubOp(), input) ;
        qIter = new QueryIterSlice(qIter, opSlice.getStart(), opSlice.getLength(), execCxt) ;
        return qIter ;
    }
    
    protected QueryIterator execute(OpGroupAgg opGroupAgg, QueryIterator input)
    { 
        QueryIterator qIter = executeOp(opGroupAgg.getSubOp(), input) ;
        qIter = new QueryIterGroup(qIter, opGroupAgg.getGroupVars(), opGroupAgg.getAggregators(), execCxt) ;
        return qIter ;
    }
    
    protected QueryIterator execute(OpDistinct opDistinct, QueryIterator input)
    {
        QueryIterator qIter = executeOp(opDistinct.getSubOp(), input) ;
        qIter = new QueryIterDistinct(qIter, execCxt) ;
        return qIter ;
    }

    protected QueryIterator execute(OpReduced opReduced, QueryIterator input)
    {
        QueryIterator qIter = executeOp(opReduced.getSubOp(), input) ;
        qIter = new QueryIterReduced(qIter, execCxt) ;
        return qIter ;
    }

    protected QueryIterator execute(OpAssign opAssign, QueryIterator input)
    {
        // Need prepare?
        QueryIterator qIter = executeOp(opAssign.getSubOp(), input) ;
        qIter = new QueryIterAssign(qIter, opAssign.getVarExprList(), execCxt) ;
        return qIter ;
    }

    public static QueryIterator createRootQueryIterator(ExecutionContext execCxt)
    {
        return QueryIterRoot.create(execCxt) ;
    }

    protected QueryIterator root()
    { return createRootQueryIterator(execCxt) ; }
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