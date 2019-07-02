package oul.update;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oul.manage.OULException;
import oul.precondition.Precondition;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.UpdateVisitor;
import com.hp.hpl.jena.sparql.modify.op.Update;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

public class LoopUpdate extends Update {

	Precondition precond;
	List actions; //elements of type Update
	
	public LoopUpdate( Precondition precond , List actions ){
		this.precond = precond;
		this.actions = actions;
	}
	
	public void visit(UpdateVisitor visitor) {
		visitor.visit(this);		
	}
	
	public Precondition getPrecondition(){
		return precond;
	}
	
	public Iterator getActionsIterator(){
		return actions.iterator();
	}
	

	public List getUpdates(Model model, Graph change, QuerySolution qs) {
		List actUpdates = new LinkedList();//entries of type Update
		// evaluate precondition and get possible bindings
		try {
			List bindings = precond.evaluateCondition( model , change, qs);
			// for each binding, add the actions (with variables replaced) to the Update list
			Iterator bindingsIter = bindings.iterator();
			while( bindingsIter.hasNext() ){
				Binding currBinding = (Binding)bindingsIter.next();
				Iterator actionsIter = actions.iterator();
				while( actionsIter.hasNext() ){
					Update currUpdate = (Update)actionsIter.next();
					String updateAsString = currUpdate.toString();
					Iterator varIter = currBinding.vars();
					UpdateRequest ur = new UpdateRequest();
					while( varIter.hasNext() ){
						Var v = (Var)varIter.next();
						updateAsString = updateAsString.replace( "?" + v.getName() , "<" + currBinding.get(v).getURI() + ">");
						ur = UpdateFactory.create(updateAsString);			
					}
					actUpdates.addAll(ur.getUpdates());
				}
			}
		} catch (OULException e) {
			e.printStackTrace();
		}
		return actUpdates;
	}

}
