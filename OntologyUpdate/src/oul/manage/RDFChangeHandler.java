package oul.manage;

import java.util.List;

import oul.precondition.Precondition;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;


/**
 * Class for defining change handlers for RDF
 * @author uhe
 *
 */
public class RDFChangeHandler extends ChangeHandler {
	
	public RDFChangeHandler(){
		super();
	}

	public RDFChangeHandler( boolean isAddHandler , Query pattern , Precondition precondition , List actions , boolean isUnique ){
		this.isAddHandler = isAddHandler;
		this.pattern = pattern;
		this.precondition = precondition;
		this.actions = actions;
		this.isUnique = isUnique;
		this.bindingsCombined = false;
	}
	
	/**
	 * A change is matched by a change handler if querying with the change pattern against the change graph yields a match
	 */
	public boolean matches( Graph change ){
		GraphStore g = GraphStoreFactory.create(change);
		Dataset ds = g.toDataset();
		QueryExecution qe = QueryExecutionFactory.create(pattern, ds);
		ResultSet patterns = qe.execSelect();
		boolean matches = patterns.hasNext();
		if( ! matches ){
			return false;
		}
		patternMatches = patterns.nextSolution();
		if( isUnique ){
			matches &= !patterns.hasNext();
		}
		return matches;
	}

//	private static Collection<Triple> subst(Graph change, Binding b)
//    {
//		Model m = ModelFactory.createModelForGraph(change);
//		BindingUtils.substituteIntoTriple(t, binding);
//		Set<Triple> acc = new HashSet<Triple>() ;
//        Map bNodeMap = new HashMap() ;
//        change.subst(acc, bNodeMap, b) ;        
//        return acc ;
//    }

	
}
