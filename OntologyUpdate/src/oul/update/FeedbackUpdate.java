package oul.update;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.UpdateVisitor;
import com.hp.hpl.jena.sparql.modify.op.Update;

public class FeedbackUpdate extends Update {
	
	private String feedback;
	
	public FeedbackUpdate( String f ){
		feedback = f;
	}
	
	public void visit(UpdateVisitor v) {
		v.visit(this);
	}

	
	public String getFeedback( Binding b ) {
		String currFeedback = feedback;
		int lastIndex = 0;
		int currIndex;
		if( b!=null ){
			Iterator vars = b.vars();
			while( vars.hasNext() ){
				Var v = (Var)vars.next();
				String varName = v.getVarName();
				currFeedback = currFeedback.replace("?" + varName, b.get(v).getLocalName());
			}
		}
		return currFeedback;
	}

}
