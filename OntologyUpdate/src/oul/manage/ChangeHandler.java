package oul.manage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oul.precondition.EmptyPrecondition;
import oul.precondition.Precondition;
import oul.update.ApplyThisUpdate;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.modify.op.Update;
import com.hp.hpl.jena.sparql.modify.op.UpdateDelete;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsert;

/**
 * @author uhe
 * @brief class representing a change handler
 * Change handlers are defined by an update pattern, a precondition, a list of actions.
 */
public abstract class ChangeHandler {

	protected boolean isAddHandler;

	protected boolean isUnique;
	protected Query pattern;
	protected Precondition precondition;
	protected List actions;
	protected QuerySolution patternMatches;
	private Binding precondMatches;
	protected boolean bindingsCombined;

	public ChangeHandler() {
		super();
		pattern = null;
		precondition = null;
		actions = null;
		patternMatches = null;
		isUnique = false;
		bindingsCombined = false;
	}

	/**
	 * Check whether a change matches the precondition defined by this changehandler
	 * @param change the submitted change
	 * @return true if this changehandler is applicable
	 */
	public abstract boolean matches(Graph change);
	
	/**
	 * @return true if this change handler is triggered by add updates
	 */
	public boolean isAddHandler() {
		return isAddHandler;
	}

	/**
	 * Set the change pattern for this changehandler
	 * @param q the new change pattern
	 */
	public void setPattern(Query q) {
		pattern = q;
	}

	/**
	 * Set the precondition for this changehandler
	 * @param p The new precondition
	 */
	public void setPrecondition(Precondition p) {
		precondition = p;
	}

	/**
	 * Check whether the submitted change together with the current knowledge base fulfill the preconditions of this changehandler
	 * @param model The current knowledge base
	 * @param change The submitted change
	 * @return true if precondition is fulfilled
	 */
	public boolean preconditionFulfilled( Model model, Graph change) {
		if( precondition instanceof EmptyPrecondition ){
			return true;
		}
		precondition.setChangeHandler(this);
		List res = null;
		try {
			res =  precondition.evaluateCondition( model, change, patternMatches);
		} catch (OULException e) {
			e.printStackTrace();
		}
		if( res == null || res.isEmpty() ){
			return false;
		}
		precondMatches = (Binding)res.get(0);
		return true;
	}

	/**
	 * Get the list of updates that have to be performed when this change handler is applied, replace applyThis Update with list of actual updates
	 * @param change the submitted change
	 * @return a list of actions that have to be applied in order to execute the change handler
	 */
	public List getUpdates(Graph change) {
			List actActions = new LinkedList();
			Iterator iter = actions.iterator();
			while(iter.hasNext()){
				Update u = (Update)iter.next();
				if( u instanceof ApplyThisUpdate ){
					if( isAddHandler ){
						actActions.add( new UpdateInsert(change) );
					} else {
						actActions.add( new UpdateDelete(change) );
					}
				} else {
					actActions.add(u);
				}
	//			if( u instanceof UpdateModify){
	//				UpdateModify u2 = (UpdateModify)
	//			}
	//			if( u instanceof FeedbackUpdate ){
	//				
	//			}
			}
			return actActions;
		}

	/**
	 * Get the parameter binding with which the submitted change and the precondition are matched,
	 * i.e. the values of the variables in the list of actions to be executed
	 * @return a parameter binding containing the values of parameters in the change pattern and in the precondition
	 */
	public Binding getBinding() {
		if ( !bindingsCombined ){
			bindingsCombined = true;
			if( precondMatches == null ){
				precondMatches = new BindingMap(null);
			}
			BindingUtils.addToBinding(precondMatches, patternMatches);
		}
		return precondMatches;
	}

}