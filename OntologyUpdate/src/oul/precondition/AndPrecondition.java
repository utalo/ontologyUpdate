package oul.precondition;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oul.manage.OULException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class AndPrecondition extends Precondition {

	Precondition precond1;
	Precondition precond2;

	public AndPrecondition(){
		super( Precondition.AND );
	}

	public AndPrecondition( Precondition condition1 , Precondition condition2 ){
		super( Precondition.AND );
		precond1 = condition1;
		precond2 = condition2;
	}

	public List evaluateCondition(Model model, Graph change, QuerySolution qs) throws OULException {
		List bind1 = precond1.evaluateCondition(model, change, qs);
		List bind2 = precond2.evaluateCondition(model, change, qs);
		List bindings = new LinkedList();
		for( Iterator iter1 = bind1.iterator() ; iter1.hasNext() ; ){
			Binding b1 = (Binding)iter1.next();
			for( Iterator iter2 = bind2.iterator() ; iter2.hasNext() ; ){
				Binding b2 = (Binding)iter2.next();
				if( combineBindings(b1,b2) != null )
					bindings.add(combineBindings(b1,b2));
			}
		}
		return null;
	}

	private Binding combineBindings(Binding b1, Binding b2) {
		Iterator b2Iter = b2.vars();
		while( b2Iter.hasNext() ){
			Var v = (Var)b2Iter.next();
			if( b1.contains(v) ){
				if ( b1.get(v)!=b2.get(v)){
					return null;
				}						
			} else {
				b1.add(v, b2.get(v));
			}
		}
		return b1;
	}

}
