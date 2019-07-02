package oul.precondition;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class RDFChangedPrecondition extends ChangedPrecondition {

	public RDFChangedPrecondition(Query query) {
		super( query );
	}

	protected Model getInferenceModel(Model model) {
		return ModelFactory.createRDFSModel( model );
	}

}
