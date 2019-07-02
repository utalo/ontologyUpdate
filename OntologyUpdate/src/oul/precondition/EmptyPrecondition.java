package oul.precondition;

import java.util.LinkedList;
import java.util.List;

import oul.manage.OULException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;

public class EmptyPrecondition extends Precondition {

	public EmptyPrecondition(){}
	
	public List evaluateCondition(Model model, Graph change, QuerySolution qs) throws OULException {
		return new LinkedList();
	}

}
