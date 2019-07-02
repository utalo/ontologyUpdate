package examples;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import oul.manage.OulUpdateAction;
import oul.manage.UpdateManager;


import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.modify.op.UpdateDeleteData;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.util.FileManager;

public class Example2 {
	public static void execute(){
		
		//read model
		String modelFileName ="C:/Dokumente und Einstellungen/uhe/workspace/OntologyUpdate/src/examples/example2.rdf";
		Model m = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open( modelFileName );
		if ( in == null ){
			throw new IllegalArgumentException("File: " + modelFileName + " not found.");
		}
		m.read(in, "");
		
		//read changehandlers
		String chFileName = "C:/Dokumente und Einstellungen/uhe/workspace/OntologyUpdate/src/examples/example2.oul";
		
		//create an update manager
		GraphStore gs = GraphStoreFactory.create(m);
		UpdateManager um = new UpdateManager(gs.getDefaultGraph());
		gs.getDefaultGraph().getPrefixMapping().setNsPrefix("aifb", "http://aifb/");
		um.readChangeHandler( chFileName );
		
		//Graph before change
		System.out.println("Graph before change:");
		System.out.println( m.getGraph().toString() );
		
		//create graph describing the change 
		String nsAifb = "aifb:";
		Graph change = Factory.createDefaultGraph(); 
		change.getPrefixMapping().setNsPrefix("aifb", "http://aifb/");
		//Node nPhilipp = NodeCreateUtils.create(nsAifb + "Philipp");
		//Node nHasAffil = NodeCreateUtils.create(nsAifb + "hasAffiliation");
		//Node nAifb = NodeCreateUtils.create(nsAifb + "Aifb");
		change.add(Triple.create("http://aifb/Philipp http://aifb/hasAffiliation http://aifb/Aifb"));
		//Triple.create(nPhilipp,nHasAffil,nAifb));
		
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
	}
}
