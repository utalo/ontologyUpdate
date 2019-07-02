package examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.modify.op.Update;
import com.hp.hpl.jena.sparql.modify.op.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.op.UpdateDeleteData;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsert;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsertData;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.vocabulary.RDF;
/**
 * 
 * @author Victoria
 *
 */
public class ExampleX {
	Model m;
    String nsAifb = "http://aifb/";
   
    // Resources
    Resource philipp;
    Resource aifb ;
    Resource xmedia ;
    Resource multipla ;
    Resource thanh ;
    // Types
    Resource project ;
    Resource phd; 
    Resource phdStudent;
    Resource employer;
    
    // Properties
    Property hasAffiliation ;
    Property leads ;
    Property worksOn ;
    Property supervises ;
    Property assocInstitution ;
    
    // special Properties for phd and phdStudent (ADD) 
    Property hasPhoneNumber; 
    Property hasBureau; 
    // special Properties for project (ADD)
    Property hasBudget;
    Property hasRuntime; 
    // special Propery for phdStudent
    Property hasTheme; 
    
    Resource subj;
    Resource obj; 
    Property prop; 
    
    GraphStore gs ;
    UpdateManager um ;
	  
    Query pattern;
    Precondition precond = new EmptyPrecondition();
    LinkedList actions = new LinkedList();
    ChangeHandler ch;    
    
	ExampleX(){
		// Constructor to build a model
        m = ModelFactory.createDefaultModel();
      
        nsAifb = "http://aifb/";
        
        philipp = m.createResource(nsAifb + "philipp");
        aifb = m.createResource(nsAifb + "aifb");
        xmedia = m.createResource(nsAifb + "xmedia");
        multipla = m.createResource(nsAifb + "multipla");
        thanh = m.createResource(nsAifb + "thanh");
        
        project = m.createResource(nsAifb + "project");
        phdStudent = m.createResource(nsAifb + "phdStudent");
        phd = m.createResource(nsAifb + "phd"); 
        employer =m.createResource (nsAifb + "employer");

        hasAffiliation = m.createProperty(nsAifb + "hasAffiliation");
        leads = m.createProperty(nsAifb + "leads");
        worksOn = m.createProperty(nsAifb + "worksOn");
        supervises = m.createProperty(nsAifb + "supervises");
        assocInstitution = m.createProperty(nsAifb + "assocInstitution");
        hasPhoneNumber = m.createProperty(nsAifb + "hasPhoneNumber");
        hasBureau = m.createProperty(nsAifb + "hasBureau");
        hasBudget = m.createProperty(nsAifb + "hasBudget");
        hasRuntime = m.createProperty(nsAifb + "hasRuntime");
        hasTheme = m.createProperty(nsAifb + "hasTheme");
       
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
        m.add(aifb, RDF.type, employer);
        
         //create an update manager
        gs = GraphStoreFactory.create(m);
        um = new UpdateManager(gs.getDefaultGraph());
     
	}    
    
    public void executeDel ()  throws IOException {  
    	
    	//negation of triple 
    	System.out.println();
    	System.out.println(subj.getLocalName()+ " does not "+prop.getLocalName()+" "+obj.getLocalName()+" anymore");
    	
    	//definition of query pattern 
    	pattern = QueryFactory.create("SELECT ?x ?y where {?x ?wol ?y}");//query pattern 
        precond = new EmptyPrecondition();
    	
    	actions.add(new ApplyThisUpdate());
    	actions.add(new FeedbackUpdate("?x does not "+prop.getLocalName()+" ?y anymore"));
    	
    	tripleQueryDel();  // queries 
      	// isAddHandler=false 	
    	ch = new RDFChangeHandler(false, pattern, precond, actions, false);
    	um.addChangeHandler(ch);
    	
        //create graph describing the change
        Graph change = Factory.createDefaultGraph();   
        Node n1 = NodeCreateUtils.create(subj.toString());
        Node n2 = NodeCreateUtils.create(prop.toString());
        Node n3 = NodeCreateUtils.create(obj.toString());
        change.add(Triple.create(n1,n2,n3));   
        
        UpdateDeleteData udd = new UpdateDeleteData();
        // UpdateInsertData udd =new UpdateInsertData();
        udd.setData(change);        
        
        System.out.println("Change:");
        System.out.println(udd.toString());	//  REMOVE{....} statement
  
        OulUpdateAction.setManager(um);		
        OulUpdateAction.execute(udd, gs);
        
        System.out.println();
        System.out.println("FEEDBACK:");
        System.out.println(OulUpdateAction.getFeedback()); //  FEEDBACK from Actionlist 
        
        // new graph 
		int i = 1;
		StmtIterator itSt= m.listStatements();  // counter of Statements
		Statement st;

		System.out.println("Tripels of Graph after Change: ");
		System.out.println("------------------------------ ");

		while (itSt.hasNext()){			
			System.out.println(itSt.nextStatement());
			i = i + 1;
		}
    }
    
	public void executeAdd() throws IOException{
		int i;
    	
    	pattern = QueryFactory.create("SELECT ?x ?y where {?x <"+prop+"> ?y}");//query pattern 
    	precond = new EmptyPrecondition();
    	
    	actions.add(new ApplyThisUpdate());
    	actions.add(new FeedbackUpdate("?x does "+prop.getLocalName()+" ?y now"));
    	
    	tripleQueryAdd();  // queries 
    	
    	//isAddHandler=true 
    	ch = new RDFChangeHandler(true, pattern, precond, actions, false);
    	um.addChangeHandler(ch);
    	
        //create graph describing the change
        Graph change = Factory.createDefaultGraph();   
        Node n1 = NodeCreateUtils.create(subj.toString());
        Node n2 = NodeCreateUtils.create(prop.toString());
        Node n3 = NodeCreateUtils.create(obj.toString());
        change.add(Triple.create(n1,n2,n3));
        
        UpdateInsertData uid = new UpdateInsertData();
        uid.setData(change);
        System.out.println("-------------------------");
        System.out.println("Change:");
        System.out.println(uid.toString());	//ADD{....} statement
  
        OulUpdateAction.setManager(um);		
        OulUpdateAction.execute(uid,gs);

        System.out.println("Graph after change:");
        System.out.println(gs.getDefaultGraph().toString());
        
        System.out.println();
        System.out.println("FEEDBACK:");
        System.out.println(OulUpdateAction.getFeedback()); //  FEEDBACK from Actionlist 
        
        // new graph 
		StmtIterator itSt=m.listStatements();  // counter of Statements

		System.out.println("Triples of Graph after Change: ");
		System.out.println("---------------------- ");
		
		itSt= m.listStatements();   
		i=0;
		while (itSt.hasNext()){			
			System.out.println(itSt.nextStatement());
			i = i + 1;
		} 
	}
	
    public void tripleQueryAdd() throws IOException {	 
    	Resource r, r1, r2;
    	boolean correctInput;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String read = "";
		int number=0;
		boolean isSubNew = false;
		boolean isObjNew = false;
		char ans1, ans2;
     	
    	// checks if subject is a new Resource 
    	r = checkResource(subj.toString());
    	
    	if (r == null){    // new Resource
       		r = m.createResource(nsAifb+subj.getLocalName());	// create resource
       		isSubNew = true;
       		correctInput = false;
    		while(correctInput == false){
    	   		System.out.println(subj.getLocalName()+" is a new Resource. Please select type: (1-phd, 2-phdStudent, 3-project, 4-employer):" );
    	   		read = in.readLine();
    			try {
    				number = Integer.parseInt(read);
    				if (number <= 5 && number > 0) {		
    					correctInput = true;
    				} else {
    					System.out.println("Wrong number, try again.");
    				}
    			}
    			catch (NumberFormatException ex) 
    			{ 
		    		System.out.println("Only numbers from 1 to 4 are allowed. Try again.");
				}
		    } 		
    		switch (number){		// add type-Triple
    			case 1: m.add(r, RDF.type, phd); break;
    			case 2: m.add(r, RDF.type, phdStudent); break;
    			case 3: m.add(r, RDF.type, project); break;
    			case 4: m.add(r, RDF.type, employer); break;
    		}
    	}

    	// checks if object is new Resource
    	r =checkResource(obj.toString());
    	if (r == null){    // new Resource
       		r = m.createResource(nsAifb+obj.getLocalName());	// create resource
       		isObjNew = true;
       		correctInput = false;
    		while(correctInput == false){
    	   		System.out.println(obj.getLocalName()+" is a new Resource. Please select  type: (1-phd, 2-phdStudent, 3-project, 4-employer):" );
    	   		read = in.readLine();
    			try {
    				number = Integer.parseInt(read);
    				if (number <= 5 && number > 0) {		
    					correctInput = true;
    				} else {
    					System.out.println("Wrong number, try again.");
    				}
    			}
    			catch (NumberFormatException ex) 
    			{ 
		    		System.out.println("Only numbers from 1 to 4 are allowed. Try again.");
				}
		    } 		
    		switch (number){		// add type-Triple
    			case 1: m.add(r, RDF.type, phd); break;
    			case 2: m.add(r, RDF.type, phdStudent); break;
    			case 3: m.add(r, RDF.type, project); break;
    			case 4: m.add(r, RDF.type, employer); break;
    		}
    	}
     	
    	// complete Affiliation data
    	if(prop.getLocalName().contains("hasAffiliation")){
    		// Phonenumber
      		if (m.contains(subj, hasPhoneNumber)== false) {
        		System.out.println("Please enter Phonenumber of "+subj.toString()+":");
        		read = in.readLine();
        		r=m.createResource(nsAifb+read);
        		m.add(subj, hasPhoneNumber,r);   			
    		}
    		// Bureaunumber
      		if (m.contains(subj, hasBureau)== false) {
        		System.out.println("Please enter Bureaunumber of "+subj.toString()+":");
        		read = in.readLine();
        		r = m.createResource(nsAifb+read);
        		m.add(subj, hasBureau, r);   			
    		}
     		
    		// working on any projects 
    		System.out.println("Does "+subj.getLocalName() +" work on any already existing projects? (Y/N)?");  
    		ans1 = readchar(); 
    		if (ans1=='Y'){
    			correctInput = false;
    			while (correctInput == false){
    				System.out.println("Enter project (end of input with empty String): ");
    				read = in.readLine();
    				if (read.compareTo("")==0) {
    					correctInput = true; 
    				} else {
    					read = nsAifb+read;
    					r = checkResource(read);
    					if (r != null){
							r1 = getType(r);
							if (r1 != null){
								if (getType(r).toString().compareTo(project.toString())== 0 ){
									m.add(subj, worksOn, r);
								}
    						}
    					}
    				}
    			}
    		}
    	}
    
    	// checks if there is already leads when inserting worksOn
    	if(prop.getLocalName().contains("worksOn")){
    		if (m.contains(subj, leads, obj)){
    			System.out.println();
    			System.out.println(subj.getLocalName().toString() +" is already leader of "+obj.getLocalName().toString());
       			System.out.println("Do you really want to add the triple --"+subj.getLocalName() +" "+prop.getLocalName()+" "+ obj.getLocalName()+" (Y/N) ?");
       			ans1= readchar();
       			if (ans1=='N') System.exit(0);
    		}
     	}
    	 // checks if there is already worksOn when inserting leads
    	if(prop.getLocalName().contains("leads")){
    		if (m.contains(subj, worksOn, obj)){
    			System.out.println();
    			System.out.println("There is a triple --"+subj.getLocalName().toString() +" worksOn "+obj.getLocalName().toString());
       			System.out.println("Do you want to REPLACE this triple by the triple --"+subj.getLocalName() +" "+prop.getLocalName()+" "+ obj.getLocalName()+"-- (Y/N) ?");
       			ans1= readchar();
       			if (ans1=='Y') {
       				m.remove(subj, worksOn, obj);
       			} else{
           			System.out.println("Do you want to ADD the new triple (Y/N) ?");
      				ans2 = readchar();
          			if (ans1=='N') System.exit(0);
      			}
    		}
    	}    	
    	
    	// phdStudent
    	r = checkResource(subj.toString());
    	if (r != null){
    		r1 = getType(r);
    		if (r1 != null) {
    			if (r1.toString().compareTo(phdStudent.toString())== 0 ){
    				if (m.contains(subj, hasTheme)== false){
    					System.out.println("Enter Theme of phd Thesis of "+subj.getLocalName()+" : ");
    					read = in.readLine();
    					r2 = m.createResource(nsAifb+read);
    					m.add(subj, hasTheme, r2);
    				}				
				}
    		}
    	}
 
    	//type of subject or object is project, enter Budget and runTime
       	r = checkResource(subj.toString());
    	if (r != null){
    		r1 = getType(r);
     		if (r1 != null) {
    			if (r1.toString().compareTo(project.toString())== 0 ){
    				if (m.contains(subj, hasBudget)== false){
    					System.out.println("Enter Budget of project "+subj.getLocalName()+" : ");
    					read = in.readLine();
    					r2 = m.createResource(nsAifb+read);
    					m.add(subj, hasBudget, r2);
    				}				
       				if (m.contains(subj, hasRuntime)== false){
    					System.out.println("Enter Runtime of project "+subj.getLocalName()+" : ");
    					read = in.readLine();
    					r2 = m.createResource(nsAifb+read);
    					m.add(subj, hasRuntime, r2);
    				}				
				}
    		}
    	}
       	r = checkResource(obj.toString());
    	if (r != null){
    		r1 = getType(r);
     		if (r1 != null) {
    			if (r1.toString().compareTo(project.toString())== 0 ){
    				if (m.contains(obj, hasBudget)== false){
    					System.out.println("Enter Budget of project "+obj.getLocalName()+" : ");
    					read = in.readLine();
    					r2 = m.createResource(nsAifb+read);
    					m.add(obj, hasBudget, r2);
    				}				
       				if (m.contains(obj, hasRuntime)== false){
    					System.out.println("Enter Runtime of project "+obj.getLocalName()+" : ");
    					read = in.readLine();
    					r2 = m.createResource(nsAifb+read);
    					m.add(obj, hasRuntime, r2);
    				}				
				}
    		}
    	}
 	
    }
	public void tripleQueryDel() throws IOException {		//delete 
		char ans1;
		char ans2;
		char ans3; 
		String nsAifb = "http://aifb/";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String read = "";
		boolean correctInput;
		
		System.out.println("Why do you want to delete this triple?");

		// hasaffiliation
		if (prop.getLocalName().contains("hasAffiliation")) {
			System.out.println("Was affiliation finished? (Y/N)");
			ans1 = readchar();
			if (ans1 == 'N') {
				System.out.println("Is there a new employer? (Y/N)? ");
				ans2 = readchar();
				if (ans2 == 'Y') {
					System.out.println("Enter name of new employer: ");
					read = in.readLine();
					read = nsAifb + read;
					Resource r = checkResource(read);
					if (r != null) {
						System.out.println("Resource already exists");
					} else {
						System.out.println("Insert new Resource "+ read);
					    r = m.createResource(read);
					}
					m.add(subj, prop,r);  // ADD ohne changehandler?!?!?
				}
			}
		}
		
		// worksOn
		if (prop.getLocalName().contains("worksOn")) {
			System.out.println("Was the cooperation finished? (Y,N)");
			ans1 = readchar();
			if (ans1 == 'N') {
				System.out.println("Is "+subj.getLocalName()+" now leader of "+obj.getLocalName()+"? (Y,N)");				
				ans2=readchar(); 
				if (ans2 == 'Y') {
					m.add(subj,leads,obj); 
				}
			}
		}
		
		// leads
		if (prop.getLocalName().contains("leads")) {
			System.out.println("Was leading finished? (Y,N)");
			ans1 = readchar();
			if (ans1 == 'Y') {
				System.out.println("Does "+subj.getLocalName() +" already work on "+obj.getLocalName()+"? (Y,N)");
				ans2 = readchar();
				if (ans2 == 'Y') {
					m.add(subj, worksOn, obj);  
				}
			}
		}
		
		// supervises
		if (prop.getLocalName().contains("supervises")) {
			System.out.println("Was supervision of "+ obj.getLocalName()+ " finished? (Y,N)");
			ans1 = readchar();
			if (ans1 == 'N') {
				System.out.println("Does "+obj.getLocalName()+" have a new supervisor? (Y,N)");
				ans2 = readchar();
				if (ans2 == 'Y') {
					System.out.println("Enter the name of the new supervisor: ");
					read = in.readLine();
					read = nsAifb + read;
					Resource r = checkResource(read);
					if (r != null) {
						System.out.println("Resource already exists");
					} else {
						System.out.println("Insert new Resource "+ read);
					    r = m.createResource(read);
					}
					m.add(r, prop,obj);  
				}
			}
		}
		
		// subject or object is reason
		// phdStudent
		if (obj.getLocalName().contains("phdStudent")) {
			System.out.println("Has "+subj.getLocalName()+" canceled his phd-study? (Y/N)");
			ans1 = readchar();
			if (ans1 == 'N') {
				System.out.println("Has "+subj.getLocalName()+" finished his phd-study? (Y/N)");
				ans2 = readchar();
				if (ans2 == 'Y') {
					m.add(subj, RDF.type , phd);  
				}
			}
		}

		// subject or object are of type phd or phdstudent
		Resource r = getType(subj);
		if (r != null){
			if (getType(subj).toString().compareTo(phd.toString())== 0 || getType(subj).toString().compareTo(phdStudent.toString())== 0 ){
				System.out.println("Was "+subj.getLocalName()+ " cancelled or has he finished work for himself? (Y/N)");
				ans1 = readchar();
				if (ans1 == 'Y') {
					preDelAllSubj();  
				}
			}	
		}
		r = getType(obj);
		if (r != null){
			if (getType(obj).toString().compareTo(phd.toString())== 0 || getType(obj).toString().compareTo(phdStudent.toString())== 0 ){
				System.out.println("Was "+obj.getLocalName()+" cancelled or hes he finished work for himself? (Y/N)");
				ans1 = readchar();
				if (ans1 == 'Y') {
					preDelAllObj();  
				}
			}	
		}			
		
		// type project
		r = getType(subj);
		if (r != null){
			if (getType(subj).toString().compareTo(project.toString())== 0 ){
				System.out.println("Was Project "+subj.toString()+" finished? (Y,N)");
				ans1 = readchar();
				if (ans1 == 'Y') {
					preDelAllSubj();  // deletes all all triples where appears a subject of type phd or pd
				}
			}
		}
		r = getType(obj);
		if (r != null){
			if (getType(obj).toString().compareTo(project.toString())== 0 ){
				System.out.println("Was Project "+obj.toString()+" finished? (Y,N)");
				ans1 = readchar();
				if (ans1 == 'Y') {
					preDelAllObj();   // deletes all all triples where appears a object of type phd or pd
				}
			}
		}
	}
	
	public char readchar() {
		BufferedReader in;
		String read;
		in = new BufferedReader(new InputStreamReader(System.in));
		try {
			read = in.readLine();
			if (read.equals("Y") | read.equals("y")) {
				return 'Y';
			} else {
				return 'N';
			}
		} catch (IOException e) {
			System.out.println("The number could not be parsed.");
			e.printStackTrace();
		}
		return 'N';
	}

	public Resource checkResource(String s){
		// checks if String s is a existing Resource (s contains http//...)
		// and return the corresponding Resource or Null if s doesn't exist
		StmtIterator itSt= m.listStatements();  // counter of Statements
		Statement st;
		
		while (itSt.hasNext()){			// when found, return Resource
			st = itSt.nextStatement();
//			System.out.println(s+" "+st.getSubject().toString()+"  "+st.getResource().toString());
			if (st.getSubject().toString().compareTo(s)==0) return st.getSubject();
			if (st.getResource().toString().compareTo(s)==0) return st.getResource();			
		}
		return null;	// when not found, return null
	}
	
	public Resource getType(Resource s){
		// checks if there is a type-Triple to the Resource s
		// and returns the Type if exists or return Null if there is no such triple
		StmtIterator itSt= m.listStatements();  // counter of Statements
		Statement st;
		
		while (itSt.hasNext()){			// when found, return type
			st = itSt.nextStatement();
			if (st.getSubject().toString().compareTo(s.toString()) == 0 && st.getPredicate().toString().compareTo(RDF.type.toString())==0 ) return st.getResource();					
		}
		return null;	// when not found, return null
	}
	
	// Preconditions
	
	public void preDelAllSubj(){
		
		Query pre1 = QueryFactory.create("Select ?x ?a ?w where" + "{?x ?a ?w}");
		Precondition loop1pre = new SyntacticPrecondition(pre1);
		LinkedList loop1Actions = new LinkedList();
		
		loop1Actions.addAll(UpdateFactory.create(
				"DELETE { ?x ?a ?w}").getUpdates());
		loop1Actions.add(new FeedbackUpdate(
				"Thus, ?x does not ?a ?w anymore."));
		actions.add(new LoopUpdate(loop1pre, loop1Actions));
		
		// ?x as Object
		pre1 = QueryFactory.create("Select ?w ?a ?x where" + "{?w ?a ?x}");
		loop1pre = new SyntacticPrecondition(pre1);
		loop1Actions = new LinkedList();
		
		loop1Actions.addAll(UpdateFactory.create(
				"DELETE { ?w ?a ?x}").getUpdates());
		loop1Actions.add(new FeedbackUpdate(
				"Thus, ?w does not ?a ?x anymore."));
		actions.add(new LoopUpdate(loop1pre, loop1Actions));
	}	
	
	public void preDelAllObj(){
		
		// ?y as Subject
		Query pre1 = QueryFactory.create("Select ?y ?a ?w where" + "{?y ?a ?w}");
		Precondition loop1pre = new SyntacticPrecondition(pre1);
		LinkedList loop1Actions = new LinkedList();
		
		loop1Actions.addAll(UpdateFactory.create(
				"DELETE { ?y ?a ?w}").getUpdates());
		loop1Actions.add(new FeedbackUpdate(
				"Thus, ?y does not ?a ?w anymore."));
		actions.add(new LoopUpdate(loop1pre, loop1Actions));
		
		// ?x as Object
		pre1 = QueryFactory.create("Select ?w ?a ?y where" + "{?w ?a ?y}");
		loop1pre = new SyntacticPrecondition(pre1);
		loop1Actions = new LinkedList();
		
		loop1Actions.addAll(UpdateFactory.create(
				"DELETE { ?w ?a ?y}").getUpdates());
		loop1Actions.add(new FeedbackUpdate(
				"Thus, ?w does not ?a ?y anymore."));
		actions.add(new LoopUpdate(loop1pre, loop1Actions));
	}

	//TODO: Methode raus
	public void preM() { // Test
		System.out.println("preM");
		
		Query pre1 = QueryFactory.create("Select ?y ?a ?w where" + "(<"+ thanh.getURI()+"> <"+ leads.getURI()+"> <"+philipp.getURI()+">)");
		Precondition loop1pre = new SyntacticPrecondition(pre1);
		LinkedList loop1Actions = new LinkedList();
		
		loop1Actions.addAll(UpdateFactory.create(
				"INSERT ( <"+ thanh.getURI()+"> <"+ leads.getURI()+"> <"+philipp.getURI()+"> )").getUpdates());
		loop1Actions.add(new FeedbackUpdate(
				"Test INSERT."));
		actions.add(new LoopUpdate(loop1pre, loop1Actions));
		System.out.println(actions.toString());
		
	}
	public void pre1() { // worksOn,  löscht alle ?x worksOn wenn ?x überhaupt nicht mehr arbeitet
		System.out.println("pre1");
		Query pre1 = QueryFactory.create("Select ?x ?w where" + "{?x <"
				+ worksOn.getURI() + "> ?w}");
		Precondition loop1pre = new SyntacticPrecondition(pre1);
		LinkedList loop1Actions = new LinkedList();
		
		loop1Actions.addAll(UpdateFactory.create(
				"DELETE { ?x <" + worksOn.getURI() + "> ?w}").getUpdates());
		loop1Actions.add(new FeedbackUpdate(
				"Thus, ?x does not work on ?w anymore."));
		actions.add(new LoopUpdate(loop1pre, loop1Actions));
		System.out.println(actions.toString());
		}
	public void pre2() { // worksOn , löscht alle worksOn ?y, wenn ?y (Projekt als Objekt) nicht mehr existiert
		System.out.println("pre2");
		Query pre1 = QueryFactory.create("Select ?a ?y where" + "{?a <"
				+ worksOn.getURI() + "> ?y}");
		Precondition loop1pre = new SyntacticPrecondition(pre1);
		LinkedList loop1Actions = new LinkedList();
		
		loop1Actions.addAll(UpdateFactory.create(
				"DELETE { ?a <" + worksOn.getURI() + "> ?y}").getUpdates());
		loop1Actions.add(new FeedbackUpdate(
				"Thus, ?a does not work on ?y anymore."));
		actions.add(new LoopUpdate(loop1pre, loop1Actions));
	}
	public void pre3() { //  worksOn , löscht alle works on ?x wenn ?x (Projekt als Subjekt) nixht mehr existiert
		System.out.println("pre3");
		Query pre1 = QueryFactory.create("Select ?a ?x where" + "{?a <"
				+ worksOn.getURI() + "> ?x}");
		Precondition loop1pre = new SyntacticPrecondition(pre1);
		LinkedList loop1Actions = new LinkedList();
		
		loop1Actions.addAll(UpdateFactory.create(
				"DELETE { ?a <" + worksOn.getURI() + "> ?x}").getUpdates());
		loop1Actions.add(new FeedbackUpdate(
				"Thus, ?a does not work on ?x anymore."));
		actions.add(new LoopUpdate(loop1pre, loop1Actions));
	}
	
	
	public void pre4() {//  sup , löscht alle supervises ?x, wenn ?x (phd als Subjekt) nicht mehr existiert
		System.out.println("pre4");
		Query pre = QueryFactory.create("Select ?b ?x where " + "{ ?b <"
				+ supervises.getURI() + "> ?x}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?b <" + supervises.getURI() + "> ?x}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?b does not supervise ?x anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre5() { // sup, löscht alle ?x supervises,  wenn ?x (phd als Subjekt) nicht mehr existiert
		System.out.println("pre5");
		Query pre = QueryFactory.create("Select ?x ?c where " + "{ ?x <"
				+ supervises.getURI() + "> ?c}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?x <" + supervises.getURI() + "> ?c}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x does not supervise ?c anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));
		System.out.println(actions.toString());
	}
	public void pre6() { // delete affiliation, löscht alle ?x hasAffiliation,  wenn ?x (phd als Subjekt) nicht mehr existiert
		System.out.println("pre6");
		Query pre = QueryFactory.create("Select ?x ?z where " + "{ ?x <"
				+ hasAffiliation.getURI() + "> ?z}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?x <" + hasAffiliation.getURI() + "> ?z}")
				.getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x does not work for ?z anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));
		System.out.println(actions.toString());
	}
	public void pre7() {// delete assocInst
		System.out.println("pre7");
		Query pre = QueryFactory.create("SELECT ?y ?z where " + "{ ?y <"
				+ assocInstitution.getURI() + "> ?z}"); 			
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?y <" + assocInstitution.getURI() + "> ?z}")
				.getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?y is no asssocInstitution of ?z anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre8() {// delete assocInst
		System.out.println("pre8");
		Query pre = QueryFactory.create("SELECT ?x ?z where " + "{ ?x <"
				+ assocInstitution.getURI() + "> ?z}"); 			
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?x <" + assocInstitution.getURI() + "> ?z}")
				.getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x is no asssocInstitution of ?z anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre9() { // delete affiliation
		System.out.println("pre9");
		Query pre = QueryFactory.create("Select ?y ?z where " + "{ ?y <"
				+ hasAffiliation.getURI() + "> ?z}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?y <" + hasAffiliation.getURI() + "> ?z}")
				.getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?y does not work for ?z anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));	}

	public void pre10() {
		System.out.println("pre10");
		Query pre = QueryFactory.create("SELECT ?d ?x where " + "{ ?d<"
				+ leads.getURI() + "> ?x}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?d <" + leads.getURI() + "> ?x}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?d does not lead on ?x anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}

	public void pre11() {
		System.out.println("pre11");
		Query pre = QueryFactory.create("SELECT ?e ?y where " + "{ ?e <"
				+ leads.getURI() + "> ?y}");
		Precondition looppre = new SyntacticPrecondition(pre);

		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?e <" + leads.getURI() + "> ?y}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?e does not lead on ?y anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre12() {
		System.out.println("pre12");
		Query pre = QueryFactory.create("SELECT ?x " );
		Precondition looppre = new SyntacticPrecondition(pre);

		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"ADD { ?x <" + RDF.type + "> PHD}").getUpdates());//?x type PHD
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x is now PHD."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre13() {
		System.out.println("pre13");
		//TODO: oben eingegebenen Wert (neuer Leiter) hierher übergeben 
		Query pre = QueryFactory.create("SELECT ?y " );
		Precondition looppre = new SyntacticPrecondition(pre);

		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"ADD { ?n <" + leads.getURI() + "> ?y}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?n now leads ?y."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre14() {
		System.out.println("pre14");
		//TODO: oben eingegebenen Wert (neuer Betruer) hierher übergeben 
		Query pre = QueryFactory.create("SELECT ?y " );
		Precondition looppre = new SyntacticPrecondition(pre);

		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"ADD { ?n <" + supervises.getURI() + "> ?y}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?n now supervises ?y."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}	
	public void pre15() {
		System.out.println("pre15");
		//TODO: wert von oben (neuer Arbeitgeber) hier einbauen 
		Query pre = QueryFactory.create("SELECT ?x " );
		Precondition looppre = new SyntacticPrecondition(pre);

		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"ADD { ?x <" + hasAffiliation.getURI() + "> ?n}").getUpdates());//?x type PHD
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x now works for ?n ."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre16() {
		System.out.println("pre16");
		Query pre = QueryFactory.create("SELECT ?x ?y where " + "{ ?x<"
				+ worksOn.getURI() + "> ?y}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"ADD { ?x <" + leads.getURI() + "> ?y}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x does lead on ?y now."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre17() {
		System.out.println("pre17");
		Query pre = QueryFactory.create("SELECT ?x ?y where " + "{ ?x<"
				+ leads.getURI() + "> ?y}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"ADD { ?x <" + worksOn.getURI() + "> ?y}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x does work on ?y now."));
		actions.add(new LoopUpdate(looppre, loopActions));
	}
	public void pre18() {
		System.out.println("pre18");
		Query pre = QueryFactory.create("SELECT ?x ?f where " + "{ ?x<"
				+ leads.getURI() + "> ?f}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"DELETE { ?x <" + leads.getURI() + "> ?f}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x does not lead on ?f anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));
		System.out.println(actions.toString());
	}	



	/*
	public void pre18(){//Theme Phd thesis 
			Query pre = QueryFactory.create("SELECT ?x ?c where " + "{ ?x <"
					+ hasTheme.getURI() + "> ?c}"
					+"{?c=ans3}");
			Precondition looppre = new SyntacticPrecondition(pre);
			LinkedList loopActions = new LinkedList();
			loopActions.addAll(UpdateFactory.create(
					"ADD { ?x <" + hasTheme.getURI() + "> ?c}").getUpdates());
			loopActions.add(new FeedbackUpdate(
					"Thus, ?x has Theme ?c."));
			actions.add(new LoopUpdate(looppre, loopActions));		
	}
	
	public void pre19(){
			Query pre = QueryFactory.create("SELECT ?x ?z where " + "{ ?x <"
					+ hasBudget.getURI() + "> ?c}"
					+"{?c=ans4}");
			Precondition looppre = new SyntacticPrecondition(pre);
			LinkedList loopActions = new LinkedList();
			loopActions.addAll(UpdateFactory.create(
					"ADD { ?x <" + hasBudget.getURI() + "> ?c}").getUpdates());
			loopActions.add(new FeedbackUpdate(
					"Thus, ?x has Budget ?c."));
			actions.add(new LoopUpdate(looppre, loopActions));		
	} */
	public void pre20(){ 
		System.out.println("pre20");
		System.out.println("Noch nicht realisiert");
		/*
			Query pre = QueryFactory.create("SELECT ?x ?c where " + "{ ?x <"
					+ hasRuntime.getURI() + "> ?c}"
					+"{?c=ans5}");
			Precondition looppre = new SyntacticPrecondition(pre);
			LinkedList loopActions = new LinkedList();
			loopActions.addAll(UpdateFactory.create(
					"ADD { ?x <" + hasRuntime.getURI() + "> ?c}").getUpdates());
			loopActions.add(new FeedbackUpdate(
					"Thus, ?x hasRuntime ?c ."));
			actions.add(new LoopUpdate(looppre, loopActions));	*/	
	}
	public void pre21(){
		System.out.println("pre21");
		System.out.println("Noch nicht realisiert"); /*
		Query pre = QueryFactory.create("SELECT ?x ?z where " + "{ ?subj <"
				+ assocInstitution.getURI() + "> aifb}");
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		loopActions.addAll(UpdateFactory.create(
				"ADD { ?subj <" + assocInstitution.getURI() + "> aifb}").getUpdates());
		loopActions.add(new FeedbackUpdate(
				"Thus, ?x does not lead on ?z anymore."));
		actions.add(new LoopUpdate(looppre, loopActions));		*/
	}

	public void pre24(){
		System.out.println("pre24");
		System.out.println("Noch nicht realisiert");
	}
	public void pre2222() { 
		System.out.println("pre22221");
		//TODO: weist außerhalb den wert für ?c zu 
	//	Query pre = QueryFactory.create("Select ?x where" + "{?x <"+hasAffiliation.getURI()+">?y}");				
		Query pre = QueryFactory.create("SELECT ?x ?y where {?x <http://aifb/hasAffiliation> ?y}");
	 
		Precondition looppre = new SyntacticPrecondition(pre);
		LinkedList loopActions = new LinkedList();
		//loopActions.addAll(UpdateFactory.create("").getUpdates());
		//SCHROTT???
		loopActions.addAll(UpdateFactory.create("ADD { <http://aifb/hasAffiliation> <" + hasBureau.getLocalName() + "> 111}").getUpdates());
		loopActions.add(new FeedbackUpdate("Thus, ?x has bureau ?z."));
		actions.add(new LoopUpdate(looppre, loopActions));		
	}
	public void pre7777(String c) {
		System.out.println("pre7777");
		System.out.println("Noch nicht realisiert");
		/*/ add Bureau
		//TODO: liest die Engine char ein oder integer?
		//TODO Büronummer je als Literal anlegen oder sonstwie speichern 
			Query pre = QueryFactory.create("SELECT ?x ?"+c+" where " + "{ ?x <"
					+ hasBureau.getURI() + "> ?"+c+"}");
					//+"{?"+c+"=ans3}");
			Precondition looppre = new SyntacticPrecondition(pre);
			LinkedList loopActions = new LinkedList();
			loopActions.addAll(UpdateFactory.create(
					"ADD { ?x <" + hasBureau.getURI() + "> ?"+c+"}").getUpdates());
			loopActions.add(new FeedbackUpdate(
					"Thus, ?x has Bureau ?"+c+" now."));
			actions.add(new LoopUpdate(looppre, loopActions));		*/
	}
}

