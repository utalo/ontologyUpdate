package oul.update;

import com.hp.hpl.jena.sparql.modify.UpdateVisitor;
import com.hp.hpl.jena.sparql.modify.op.Update;

public class ApplyThisUpdate extends Update {

	public void visit(UpdateVisitor arg0) {
		System.err.println("The method visit in ApplyThisUpdate should never have been called");
	}

}
