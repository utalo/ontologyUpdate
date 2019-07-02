package oul.precondition;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oul.manage.OULException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public abstract class ChangedPrecondition extends Precondition {

	Query condition;
	
	public ChangedPrecondition(){
		super( Precondition.SEMANTICCHANGED );
	}

	public ChangedPrecondition( Query condition ){
		super( Precondition.SEMANTICCHANGED );
		this.condition = condition;
	}
	
	public List evaluateCondition(Model model, Graph change, QuerySolution qs ) throws OULException {
		List bindings = new LinkedList();
		List changedTriples = new LinkedList();
		if( changehandler == null ){
			throw new OULException( "ChangedPrecondition does not belong to a changehandler" );
		}
		Model changeModel = ModelFactory.createModelForGraph( change );
		changeModel = changeModel.difference(model);
		
		if( changehandler.isAddHandler() ){
			model.add( changeModel );
		} else {
			model.remove( changeModel );
		}
		Model m = getInferenceModel( model) ;
		bindings = evalBindings(condition, m, qs);
		if( changehandler.isAddHandler() ){
			model.remove( changeModel );
		} else {
			model.add( changeModel );			
		}
		return bindings;
	}

	protected abstract Model getInferenceModel(Model model);

}
