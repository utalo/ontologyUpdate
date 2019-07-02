package examples;

import java.util.LinkedList;

import oul.manage.ChangeHandler;
import oul.manage.RDFChangeHandler;
import oul.manage.OulUpdateAction;
import oul.manage.UpdateManager;
import oul.precondition.EmptyPrecondition;
import oul.precondition.Precondition;
import oul.precondition.SyntacticPrecondition;
import oul.update.ApplyThisUpdate;
import oul.update.FeedbackUpdate;
import oul.update.LoopUpdate;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.modify.op.UpdateDeleteData;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.vocabulary.RDF;


public class Example1 {

	public static void execute(){
		//build a model
		Model m = ModelFactory.createDefaultModel();
		
		String nsAifb = "http://aifb/";
		Resource philipp = m.createResource(nsAifb + "philipp");
		Resource phdStudent = m.createResource(nsAifb + "PhDStudent");
		Resource aifb = m.createResource(nsAifb + "aifb");
		Resource xmedia = m.createResource(nsAifb + "xmedia");
		Resource multipla = m.createResource(nsAifb + "multipla");
		Resource thanh = m.createResource(nsAifb + "thanh");
		Resource project = m.createResource(nsAifb + "Project");
		
		Property hasAffiliation = m.createProperty(nsAifb + "hasAffiliation");
		Property leads = m.createProperty(nsAifb + "leads");
		Property worksOn = m.createProperty(nsAifb + "worksOn");
		Property supervises = m.createProperty(nsAifb + "supervises");
		Property assocInstitution = m.createProperty(nsAifb + "assocInstitution");
		
		m.add(philipp, RDF.type , phdStudent);
		m.add(philipp, hasAffiliation, aifb);
		m.add(philipp, leads, xmedia );
		m.add(philipp, worksOn, multipla);
		m.add(philipp, supervises, thanh );
		m.add(thanh, RDF.type, phdStudent);
		m.add(thanh, hasAffiliation, aifb);
		m.add(thanh, worksOn, xmedia );
		m.add(xmedia, RDF.type, project);
		m.add(xmedia, assocInstitution, aifb );
		m.add(multipla, RDF.type, project );
		m.add(multipla, assocInstitution, aifb );
		
		
		//create an update manager
		GraphStore gs = GraphStoreFactory.create(m);
		UpdateManager um = new UpdateManager(gs.getDefaultGraph());
		
		System.out.println("Graph before change:");
		System.out.println(gs.getDefaultGraph().toString());
		
		//create changehandlers
		Query pattern = QueryFactory.create("SELECT ?x ?y where {?x <http://aifb/hasAffiliation> ?y}");
		Precondition precond = new EmptyPrecondition();
		
		LinkedList actions = new LinkedList();
		actions.add(new ApplyThisUpdate());
		actions.add(new FeedbackUpdate("?x does not work at ?y anymore"));

		Query pre = QueryFactory.create("SELECT ?x ?wol ?z where "
				+"{{?z <" + RDF.type + "> <" + project.getURI() + ">."
				+ "?z <"+ assocInstitution.getURI() +"> ?y."
				+ "FILTER(?wol= <" + worksOn.getURI() + ">)} UNION {" 
				+ "?z <" + RDF.type + "> <" + project.getURI() + ">."
				+ "?z <" + assocInstitution.getURI() + "> ?y."
				+ "FILTER (?wol = <" + leads.getURI() +">)}}");
		Precondition loopPre = new SyntacticPrecondition( pre );
		
		LinkedList loopActions = new LinkedList();
		loopActions.addAll( UpdateFactory.create("DELETE { ?x ?wol ?z }").getUpdates() );
		loopActions.add(new FeedbackUpdate("Thus ?x does not lead/work on project ?z anymore."));
		
		actions.add(new LoopUpdate(loopPre,loopActions));

		Query pre2 = QueryFactory.create( "Select ?x ?z where "
				+"{ ?x <" + supervises.getURI() + "> ?z."
				+" ?z <" + hasAffiliation + "> ?y}");
		Precondition loop2pre = new SyntacticPrecondition( pre2 );

		LinkedList loop2Actions = new LinkedList();
		loop2Actions.addAll( UpdateFactory.create("DELETE { ?x <" + supervises.getURI() + "> ?z}" ).getUpdates() );
		loop2Actions.add( new FeedbackUpdate("Thus, ?x does not supervise ?z anymore."));
		
		actions.add( new LoopUpdate( loop2pre , loop2Actions ) );
		
		ChangeHandler ch = new RDFChangeHandler(false,pattern,precond,actions,false);
		um.addChangeHandler(ch);
		
		//create graph describing the change 
		Graph change = Factory.createDefaultGraph();   
		Node nPhilipp = NodeCreateUtils.create(nsAifb + "philipp");
		Node nHasAffil = NodeCreateUtils.create(nsAifb + "hasAffiliation");
		Node nAifb = NodeCreateUtils.create(nsAifb + "aifb");
		change.add(Triple.create(nPhilipp,nHasAffil,nAifb));
		
		UpdateDeleteData udd = new UpdateDeleteData();
		udd.setData(change);
		
		System.out.println("Change:");
		System.out.println(udd.toString());
		
		OulUpdateAction.setManager(um);
		OulUpdateAction.execute(udd, gs);


		System.out.println("Graph after change:");
		System.out.println(gs.getDefaultGraph().toString());
		
		System.out.println();
		System.out.println(OulUpdateAction.getFeedback());
//		CREATE CHANGEHANDLER leavesInstitution
//		FOR del { ?x hasAffiliation ?y }
//		AS applyRequest;
//		feedback("?x is no longer affiliated to ?y");
//		for(contains(?x ?wol ?z . ?z rdf:type Project .
//		?z assocInstitution ?y .
//		FILTER(?wol=worksOn || ?wol=leads)))
//		delete data {?x ?wol ?z};
//		feedback("Thus, ?x does not lead/work on project ?z anymore."); end;
//		for(contains(?x supervises ?z . ?z hasAffiliation ?y))
//		delete data {?x supervises ?z};
//		feedback("Thus, ?x does not supervise ?z anymore"); end;
		//try updates
		
	}
	
}
