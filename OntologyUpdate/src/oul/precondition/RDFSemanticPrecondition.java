package oul.precondition;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.update.GraphStoreFactory;

public class RDFSemanticPrecondition extends SemanticPrecondition {

	public RDFSemanticPrecondition(){
		super( Precondition.SEMANTIC );
	}
	
	public RDFSemanticPrecondition( Query condition ){
		super( Precondition.SEMANTIC );
		this.condition = condition;
	}
	
	public List evaluateCondition(Model model, Graph change, QuerySolution qs ) {
		List bindings = new LinkedList();
		Model rdfsModel = ModelFactory.createRDFSModel(model);
		bindings = evalBindings(condition, rdfsModel, qs );
		return bindings;
	}

}
