package oul.precondition;

import java.util.List;

import oul.manage.OULException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;

public class OrPrecondition extends Precondition {

	Precondition precond1;
	Precondition precond2;
	
	public OrPrecondition(){
		super( Precondition.OR );
	}
	
	public OrPrecondition( Precondition condition1 , Precondition condition2 ){
		super( Precondition.OR );
		precond1 = condition1;
		precond2 = condition2;
	}
	
	public List evaluateCondition(Model model, Graph change, QuerySolution qs ) throws OULException {
		List bind1 = precond1.evaluateCondition(model, change, qs);
		List bind2 = precond2.evaluateCondition(model, change, qs);
		bind1.addAll(bind2);
		return bind1;
	}

}
