package oul.precondition;

import oul.manage.ChangeHandler;

import com.hp.hpl.jena.query.Query;

public abstract class SemanticPrecondition extends Precondition {

	protected Query condition;

	public SemanticPrecondition() {
		super();
	}

	public SemanticPrecondition(int type) {
		super(type);
	}
	
	public SemanticPrecondition(int type, ChangeHandler ch) {
		super(type, ch);
	}

	public SemanticPrecondition(ChangeHandler ch) {
		super(ch);
	}

}