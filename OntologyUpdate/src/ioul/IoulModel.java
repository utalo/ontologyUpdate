/**
 * 
 */
package ioul;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;

import oul.manage.ChangeHandler;
import oul.manage.UpdateManager;
import oul.precondition.EmptyPrecondition;
import oul.precondition.Precondition;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Victoria 
 * Class of interactive OUL - model 
 */
public class IoulModel {
	
	// Class variables
	Model m;
    String nsExample = "http://aifb/"; //URI
     
    Resource subj;
    Resource obj; 
    Property prop; 
    
    GraphStore gs ;
    UpdateManager um ;
	  
    Query pattern;
    Precondition precond = new EmptyPrecondition();
    LinkedList actions = new LinkedList();
    ChangeHandler ch;    
    
    /**
     * Constructor to build the IOUL-model
     */
	IoulModel(String f){  		
        m = ModelFactory.createDefaultModel();
        
		//read model from File c:\temp\model_in.rdf        
		String modelFileName = f;
		InputStream in = FileManager.get().open( modelFileName );
		if ( in == null ){
			throw new IllegalArgumentException("File: " + modelFileName + " not found.");			
		}
		m.read(in, "");

		checkAllTypes();
		checkAllProps();
		
		//create the update manager
        gs = GraphStoreFactory.create(m);
        um = new UpdateManager(gs.getDefaultGraph());        
 	}  

	/**
	 * checks if there is a type-Triple for the Resource s, returns Type or Null
	 * if there is no type-triple 
	 */
	public Resource checkTypeTriple(Resource s) {
		StmtIterator itSt = m.listStatements(); // counter of Statements
		Statement st;

		while (itSt.hasNext()) { // when found, return type
			st = itSt.nextStatement();
			if (st.getSubject().toString().compareTo(s.toString()) == 0
					&& st.getPredicate().toString().compareTo(
							RDF.type.toString()) == 0)
				return st.getResource();
		}
		return null; // when not found, return null
	}

	/**
	 * check all triples of ontology if String s is an existing subject or object of a triple 
	 * (s contains http//...)
	 * return the corresponding Resource or Null if s does not exist
	 */
	public  Resource checkResource(String s) {
		
		StmtIterator itSt = m.listStatements(); 
		Statement st;

		while (itSt.hasNext()) { // when found, return Resource
			st = itSt.nextStatement();
			// scan all subjects
			if (st.getSubject().toString().compareTo(s) == 0)
				return st.getSubject();
			// scan all objects
			if (st.getResource().toString().compareTo(s) == 0)
				return st.getResource();
		}
		return null; // when not found, return null
	}

	/**
	 * save changed model in File modelFileName
	 */
	public void writeM(String modelFileName) {
		File ofile = new File(modelFileName);
		try {
			OutputStream out = new FileOutputStream(ofile);
			m.write(out, "RDF/XML");
		} catch (IOException ex) {
			return;
		}
	}

	/**
	 * set/get all valid types for resources 
	 * @return Types [] types
	 */
	public  Resource[] getTypes() {
		Resource[] types = new Resource[5];
		types[0] = m.createResource(nsExample + "project");
		types[1] = m.createResource(nsExample + "phdStudent");
		types[2] = m.createResource(nsExample + "phd");
		types[3] = m.createResource(nsExample + "employer");
		types[4] = m.createResource(nsExample + "literal"); 
		return types;
	}

	/**
	 * set/gett all valid properties
	 * @return Property [] props
	 */
	public  Property[] getProps() {
		Property[] props = new Property[11];
		props[0] = m.createProperty(nsExample + "hasAffiliation");
		props[1] = m.createProperty(nsExample + "leads");
		props[2] = m.createProperty(nsExample + "worksOn");
		props[3] = m.createProperty(nsExample + "supervises");
		props[4] = m.createProperty(nsExample + "assocInstitution");
		props[5] = m.createProperty(nsExample + "hasPhoneNumber");
		props[6] = m.createProperty(nsExample + "hasRoom");
		props[7] = m.createProperty(nsExample + "hasBudget");
		props[8] = m.createProperty(nsExample + "hasRuntime");
		props[9] = m.createProperty(nsExample + "hasTheme");
		props[10] = RDF.type;
		return props;
	}

	/**
	 * checks if there are invalid properties used in the model m
	 * returns property array 
	 */
	public void checkAllProps(){
		Property[] props ;
		props = getProps();
		int pend = props.length;
	    
		StmtIterator itSt= m.listStatements();  // counter of Statements
		Statement st;
		
		// check all statements
		while (itSt.hasNext()){			
			st = itSt.nextStatement();
			Property p = st.getPredicate();
			int i = 0;
			boolean found = false;
			while (found == false & i<pend){
				if (p.toString().equalsIgnoreCase(props[i].toString()) ) found = true;
				i=i+1;
			}
			if (found == false){
				System.out.println("ERROR - wrong property in RDF-file ");
				System.out.println("Property " + p.toString() + " is not allowed.");
				System.exit(0);
			}
		}	    
	}

	/**
	 * checks, if all used types in model m are valid
	 */
	public void checkAllTypes(){
		Resource [] types;
		types = getTypes();
		int tend = types.length;
		
        StmtIterator itSt= m.listStatements();  // counter of Statements
		Statement st;
		
		// check all statements
		while (itSt.hasNext()){			
			st = itSt.nextStatement();
			if ((st.getPredicate()).equals(RDF.type) ) {
				int i = 0;
				boolean found = false;
				Resource r = st.getResource();
				while (found == false & i<tend){
					if (r.toString().equalsIgnoreCase(types[i].toString()) ) found = true;
					i=i+1;
				}
				if (found == false){
					System.out.println("ERROR - wrong type in RDF-file ");
					System.out.println("Type " + r.toString()+" is not allowed.");
					System.exit(0);
				}
			}
		}
	}	
}
