package ioul;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import oul.manage.ChangeHandler;
import oul.manage.OulUpdateAction;
import oul.manage.RDFChangeHandler;
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.modify.op.UpdateDeleteData;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import ioul.IoulHelps;

/**
 * @author Victoria
 * @brief class handling the delete operation
 */
public class DelTriple {

	static IoulModel im;
	
	public static void prepareDel(IoulModel im1, String outFile) throws IOException{
		
		String question;
		int number = 0;
		boolean correctInput; 

		im = im1;
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				System.in));
		String read = "";


		int i = 1;
		StmtIterator itSt = im.m.listStatements(); // counter of Statements
		Statement st;

		// list the triples
		System.out.println();
		System.out.println("Triples of the Ontology: ");
		System.out.println("------------------------ ");

		while (itSt.hasNext()) { // list triples with numbers
			System.out.print("(" + i + ") - ");
			st = itSt.nextStatement();
			System.out.println(st);
			i = i + 1;
		}

		correctInput = false;
		while (correctInput == false) {
			question ="Please enter the number of the TRIPLE you"
							+ " want to delete, or enter the TRIPLE as String subj prop obj: ";
			read = IoulHelps.readNum(question,0,i,true);
			
			if (read.charAt(0)!= '?'){
				// Number entered
				number = Integer.parseInt(read);
				itSt = im.m.listStatements();
				int j = 0;
				while (j < number) {
					st = itSt.nextStatement();
					im.subj = st.getSubject();
					im.prop = st.getPredicate();
					im.obj = st.getResource();
					j = j + 1;
				}
				correctInput = true;
				System.out.print("You selected the Triple: ");
				System.out.println(im.subj.getLocalName() + "  "
						+ im.prop.getLocalName() + "  " + im.obj.getLocalName());
				System.out.println();
			} else {
				// Triple entered
				// Splits the entered triple
				read = read.substring(1);
				String triple[] = read.split(" "); // blank as separator in the
													// string for the 3 values

				if (triple.length != 3) { //3 words
					System.out
							.println("A triple has to consists of exactly 3 parameters.");
				} else {
					boolean found = false;
					System.out.println("Subject: " + triple[0] + "\n"
							+ "Property: " + triple[1] + "\n" + "Object: "
							+ triple[2]);
					itSt = im.m.listStatements();
					while (itSt.hasNext() && found == false) {
						// checks, if the triple is already in the ontology 
						st = itSt.nextStatement();
						im.subj = st.getSubject();
						im.prop = st.getPredicate();
						im.obj = st.getResource();

						if ((im.nsExample + triple[0]).equalsIgnoreCase(im.subj
								.toString())
								&& (im.nsExample + triple[1]).equalsIgnoreCase(im.prop
										.toString())
								&& (im.nsExample + triple[2]).equalsIgnoreCase(im.obj
										.toString())) {
							found = true;
							correctInput = true;
							System.out
									.println("You entered the valid Triple "
											+ im.subj.getLocalName()+" "
											+ im.prop.getLocalName() + " "
											+ im.obj.getLocalName());
						}
					}
					if (found == false) {
						System.out.println("Triple not found, try again");
					}
				}
			}
			if (correctInput) {
				executeDel();
				// restore outputfile
				im.writeM(outFile);
			}
		}
	}
	
	/**
	 * executes delete operation 
	 */
	public static void executeDel() throws IOException {
		
		// negation of triple
		System.out.println();
		System.out.println(im.subj.getLocalName() + " does not "
				+ im.prop.getLocalName() + " " + im.obj.getLocalName() + " anymore");

		// definition of query pattern
		im.pattern = QueryFactory.create("SELECT ?x ?y where {?x ?wol ?y}");
		
		im.precond = new EmptyPrecondition();

		im.actions.add(new ApplyThisUpdate());
		im.actions.add(new FeedbackUpdate(im.subj.getLocalName() + " does not "
				+ im.prop.getLocalName() + " " + im.obj.getLocalName() + " anymore"));

		tripleQueryDel();

		im.ch = new RDFChangeHandler(false, im.pattern, im.precond ,im.actions, false);
		im.um.addChangeHandler(im.ch);

		// create graph describing the change
		Graph change = Factory.createDefaultGraph();
		Node n1 = NodeCreateUtils.create(im.subj.toString());
		Node n2 = NodeCreateUtils.create(im.prop.toString());
		Node n3 = NodeCreateUtils.create(im.obj.toString());
		change.add(Triple.create(n1, n2, n3)); 
		
		UpdateDeleteData udd = new UpdateDeleteData();
		// UpdateInsertData uid =new UpdateInsertData();
		udd.setData(change);

		System.out.println("Change:");
		System.out.println(udd.toString()); // REMOVE{....} statement

		OulUpdateAction.setManager(im.um);
		OulUpdateAction.execute(udd, im.gs);

		System.out.println();
		System.out.println("FEEDBACK:");
		System.out.println(OulUpdateAction.getFeedback()); 

		// list new graph
		StmtIterator itSt = im.m.listStatements(); // counter of Statements

		System.out.println("Tripels of Graph after Change: ");
		System.out.println("------------------------------ ");

		while (itSt.hasNext()) {
			System.out.println(itSt.nextStatement().toString());
		}
	}
/**
 * All queries for the delete part are listed in this method. 
 */
	public static void tripleQueryDel() throws IOException { // delete
		char ans1;
		char ans2;
		String nsExample = "http://aifb/";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String read = "";
		Resource r, r1;

		System.out.println("Why do you want to delete this triple?");

		// hasaffiliation
		if (im.prop.getLocalName().equalsIgnoreCase("hasAffiliation")) {
			System.out.println("Does " + im.subj.getLocalName()
						+ " still work for " + im.obj.getLocalName()
						+ "? (Y,N)");
			ans1 = IoulHelps.readchar();
			if (ans1 == 'N') {
				System.out.println("New employer? (Y,N)? ");
				ans2 = IoulHelps.readchar();
				if (ans2 == 'Y') {
					System.out.println("Enter name of new employer: ");
					read = in.readLine();
					read = nsExample + read;
					r = im.checkResource(read);
					if (r != null) {
						System.out.println("Resource already exists");
					} else {
						System.out.println("Insert new Resource " + read);
						r = im.m.createResource(read);
					}
					r1 = im.m.createResource(nsExample + "employer");
					im.m.add(r, RDF.type, r1);
					im.actions.add(new FeedbackUpdate("   " + r.getLocalName()
							+ " is a new resource of type employer. "));
					im.m.add(im.subj, im.prop, r); 
					im.actions.add(new FeedbackUpdate("   add triple "
							+ im.subj.getLocalName() + " " + im.prop.getLocalName()
							+ " " + r.getLocalName() + ", because "
							+ im.subj.getLocalName() + " has a new employer."));
					return;  // no further questions necessary
				}
			} else {
				im.actions.add(new FeedbackUpdate(
						"   delete, because affiliation finished "));
			}
		}

		// worksOn
		if (im.prop.getLocalName().equalsIgnoreCase("worksOn")) {
			System.out.println("Project finished? (Y,N)");
			ans1 = IoulHelps.readchar();
			if (ans1 == 'N') {
				System.out.println("Is " + im.subj.getLocalName()
						+ " now leader of " + im.obj.getLocalName() + "? (Y,N)");
				ans2 = IoulHelps.readchar();
				if (ans2 == 'Y') {
					Property leads = im.m.createProperty(nsExample + "leads");
					im.m.add(im.subj, leads, im.obj); 
					im.actions.add(new FeedbackUpdate("   add triple "
							+ im.subj.getLocalName() + " leads "
							+ im.obj.getLocalName() + ", because "
							+ im.subj.getLocalName()
							+ " changes form worksOn to leads"));
					return;
				}
			} else {
				im.actions.add(new FeedbackUpdate(
						"   delete, because cooperation is completed "));
			}
		}

		// leads
		if (im.prop.getLocalName().equalsIgnoreCase("leads")) {
			System.out.println("Was leading finished? (Y,N)");
			ans1 = IoulHelps.readchar();
			if (ans1 == 'Y') {
				System.out.println("Does " + im.subj.getLocalName()
						+ " already work on " + im.obj.getLocalName() + "? (Y,N)");
				ans2 = IoulHelps.readchar();
				if (ans2 == 'Y') {
					Property worksOn = im.m.createProperty(nsExample + "worksOn");
					im.m.add(im.subj, worksOn, im.obj); 
					im.actions.add(new FeedbackUpdate("   add triple "
							+ im.subj.getLocalName() + " worksOn "
							+ im.obj.getLocalName() + ", because "
							+ im.subj.getLocalName()
							+ " changes form leads to worksOn "));
					return;
				} else {
					im.actions.add(new FeedbackUpdate(
							"   delete, because leading has finished "));
				}
			}
		}

		// supervises
		if (im.prop.getLocalName().equalsIgnoreCase("supervises")) {
			System.out.println("Was supervision of " + im.obj.getLocalName()
					+ " finished? (Y,N)");
			ans1 = IoulHelps.readchar();
			if (ans1 == 'N') {
				System.out.println("Gets " + im.obj.getLocalName()
						+ " a new supervisor? (Y,N)");
				ans2 = IoulHelps.readchar();
				if (ans2 == 'Y') {
					System.out
							.println("Enter the name of the new supervisor: ");
					read = in.readLine();
					read = nsExample + read;
					r = im.checkResource(read);
					if (r != null) {
						// System.out.println("Resource already exists");
					} else {
						// System.out.println("Insert new Resource "+ read);
						r = im.m.createResource(read);
						r1 = im.m.createResource(nsExample + "PhdStudent");
						im.m.add(r, RDF.type, r1);
						im.actions.add(new FeedbackUpdate("   " + r.getLocalName()
								+ "is a new resource of type phdStudent. "));
					}
					im.m.add(r, im.prop, im.obj); 
					im.actions.add(new FeedbackUpdate("   add triple "
							+ r.getLocalName() + " supervises "
							+ im.obj.getLocalName() + ", because "
							+ im.obj.getLocalName() + " gets a new supervisor. "));
					return;
				}
			} else {
				im.actions.add(new FeedbackUpdate(
						"   delete, because supervision has finished "));
			}
		}

		// subject or object is reason
		// phdStudent
		if (im.obj.getLocalName().equalsIgnoreCase("phdStudent")) {
			System.out.println("Has " + im.subj.getLocalName()
					+ " finished his phd-study? (Y/N)");
			ans1 = IoulHelps.readchar();			
			if (ans1 == 'Y') {
				Resource phd = im.m.createResource(nsExample + "phd");
				im.m.add(im.subj, RDF.type, phd); 
				im.actions.add(new FeedbackUpdate("   add triple "
						+ im.subj.getLocalName() + " RDF.type phd, because "
						+ im.subj.getLocalName()
						+ " has finished his phd-study. "));				
			} else {
				im.actions.add(new FeedbackUpdate(
						"   delete, because supervision has finished "));
			}
		}

		// subject or object are of type phd or phdstudent
		r = im.checkTypeTriple(im.subj);
		if (r != null) {
			if (im.checkTypeTriple(im.subj).getLocalName().equalsIgnoreCase(
					"phd")
					| im.checkTypeTriple(im.subj).getLocalName().equalsIgnoreCase("phdStudent")) {
				System.out
						.println("Has "
								+ im.subj.getLocalName()
								+ "  finished his studies? (Y/N)");
				ans1 = IoulHelps.readchar();
				if (ans1 == 'Y') {
					im.actions
							.add(new FeedbackUpdate(
									"   delete all triples where "
											+ im.subj.getLocalName()
											+ " appears as subject or object, because "
											+ im.subj.getLocalName()
											+ " has finished his studies. "));
					preDelAllSubj(); 
				}
			}
		}
		r = im.checkTypeTriple(im.obj);
		if (r != null) {
			if (im.checkTypeTriple(im.obj).getLocalName().equalsIgnoreCase(	"phd")
					| im.checkTypeTriple(im.obj).getLocalName()
							.equalsIgnoreCase("phdStudent")) {
				System.out.println("Has "	+ im.obj.getLocalName()
								+ " finished his studies? (Y/N)");
				ans1 = IoulHelps.readchar();
				if (ans1 == 'Y') {im.actions.add(new FeedbackUpdate("   delete all triples where "
											+ im.obj.getLocalName()
											+ " appears as subject or object, because "
											+ im.obj.getLocalName()
											+ " has finished his studies. "));
					preDelAllObj(); 									
				}
			}
		}

		// subject or object is of type project
		r = im.checkTypeTriple(im.subj);
		if (r != null) {
			if (im.checkTypeTriple(im.subj).getLocalName().equalsIgnoreCase(
					"project")) {
				System.out.println("Was Project " + im.subj.toString()
						+ " finished? (Y,N)");
				ans1 = IoulHelps.readchar();
				if (ans1 == 'Y') {
					im.actions
							.add(new FeedbackUpdate(
									"   delete all triples where "
											+ im.subj.getLocalName()
											+ " appears as subject or object, because project +"
											+ im.subj.getLocalName()
											+ " was finished. "));
					preDelAllSubj(); 
				}
			}
		}

		r = im.checkTypeTriple(im.obj);
		if (r != null) {
			if (im.checkTypeTriple(im.obj).getLocalName().equalsIgnoreCase(
					"project")) {
				System.out.println("Was Project " + im.obj.toString()
						+ " finished? (Y,N)");
				ans1 = IoulHelps.readchar();
				if (ans1 == 'Y') {
					im.actions
							.add(new FeedbackUpdate(
									"   delete all triples where "
											+ im.obj.getLocalName()
											+ " appears as subject or object, because project +"
											+ im.obj.getLocalName()
											+ " was finished. "));
					preDelAllObj(); 
				}
			}
		}
	}
/**
 * This method deletes all triples that contain the value of ?x from the query pattern  
 */
	public static void preDelAllSubj() {
		// ?x as subject
		Query pre1 = QueryFactory
				.create("Select ?x ?a ?w where" + "{?x ?a ?w}");
		Precondition loop1pre = new SyntacticPrecondition(pre1);
		LinkedList loop1Actions = new LinkedList();

		loop1Actions.addAll(UpdateFactory.create("DELETE { ?x ?a ?w}")
				.getUpdates());
		loop1Actions
				.add(new FeedbackUpdate("Thus, ?x does not ?a ?w anymore."));
		im.actions.add(new LoopUpdate(loop1pre, loop1Actions));

		// ?x as Object
		pre1 = QueryFactory.create("Select ?w ?a ?x where" + "{?w ?a ?x}");
		loop1pre = new SyntacticPrecondition(pre1);
		loop1Actions = new LinkedList();

		loop1Actions.addAll(UpdateFactory.create("DELETE { ?w ?a ?x}")
				.getUpdates());
		loop1Actions
				.add(new FeedbackUpdate("Thus, ?w does not ?a ?x anymore."));
		im.actions.add(new LoopUpdate(loop1pre, loop1Actions));
	}
/**
 * This method deletes all triples that contain the value of ?y from the query pattern  
 */
	public static void preDelAllObj() {
		// ?y as Subject
		Query pre1 = QueryFactory
				.create("Select ?y ?a ?w where" + "{?y ?a ?w}");
		Precondition loop1pre = new SyntacticPrecondition(pre1);
		LinkedList loop1Actions = new LinkedList();

		loop1Actions.addAll(UpdateFactory.create("DELETE { ?y ?a ?w}")
				.getUpdates());
		loop1Actions
				.add(new FeedbackUpdate("Thus, ?y does not ?a ?w anymore."));
		im.actions.add(new LoopUpdate(loop1pre, loop1Actions));

		// ?y as Object
		pre1 = QueryFactory.create("Select ?w ?a ?y where" + "{?w ?a ?y}");
		loop1pre = new SyntacticPrecondition(pre1);
		loop1Actions = new LinkedList();

		loop1Actions.addAll(UpdateFactory.create("DELETE { ?w ?a ?y}")
				.getUpdates());
		loop1Actions
				.add(new FeedbackUpdate("Thus, ?w does not ?a ?y anymore."));
		im.actions.add(new LoopUpdate(loop1pre, loop1Actions));
	}
}
