package oul.precondition;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.update.GraphStoreFactory;

public class SyntacticPrecondition extends Precondition {

	Query condition;
	
	public SyntacticPrecondition(){
		super( Precondition.SYNTACTIC );
	}
	
	public SyntacticPrecondition( Query condition ){
		super( Precondition.SYNTACTIC );
		this.condition = condition;
	}

	public List evaluateCondition(Model model, Graph change, QuerySolution qs) {
		List bindings = new LinkedList();
		bindings = evalBindings(condition, model, qs);
		return bindings;
	}
}
