/**
 * 
 */
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
import oul.update.ApplyThisUpdate;
import oul.update.FeedbackUpdate;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsertData;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Victoria
 *@brief class handling the add operation
 */
public class AddTriple {
	
	static IoulModel im;

	public static void prepareAdd(IoulModel im1, String outFile)  throws IOException {
		
		im = im1;		
		String read = "";

		Property props[];//array of properties
		Resource res[];
		Resource resNext;
		int i;
		int k;
		boolean correctInput;
		int number;
		String question;//variable for questions 

		props = new Property[(int) im.m.size()];

		System.out.println();
		System.out.println("Properties of the Ontology: ");
		System.out.println("--------------------------- ");

		boolean found;
		i = 0;
		StmtIterator itSt = im.m.listStatements();
		Statement st;

		// out.print all properties with numbers
		props = im.getProps();
		for (i=0; i<props.length; i++){
			System.out.println("(" + (i + 1) + ") - "+props[i].getLocalName());			
		}

		// choose Property by entering a Number
		correctInput = false;
		while (correctInput == false) {
			question = "Please enter the number of the PROPERTY you want to work with or enter PROPERTY as String: ";
			read = IoulHelps.readNum(question, 1, props.length, true);
			if (read.charAt(0)!= '?'){
				// Number entered as input
				number = Integer.parseInt(read);
				im.prop = props[number-1];
				System.out.print("Selected property: "
							+ im.prop.getLocalName());
				System.out.println();
				correctInput = true;

			} else {
				// String entered as input 
				found = false;
				read = read.substring(1);
				itSt = im.m.listStatements();
				while (itSt.hasNext() && found == false) {
					// checks, if entered property exists in the ontology
					st = itSt.nextStatement();

					if ((im.nsExample + read).equals(st.getPredicate().toString()) == true) {
						found = true;
						correctInput = true;
						im.prop= im.m.createProperty(im.nsExample +read);
						System.out.println("Entered Property " +im.prop.getLocalName()+" is valid.");
					}
				}
				if (found == false) {
					System.out
							.println("The entered Property is not valid, you can only enter an existing property. Try again.");
				}
			}
		}

		// chose/Enter subject and object
		itSt = im.m.listStatements();
		res = new Resource[2 * (int) im.m.size()];

		System.out.println();
		System.out.println("(Actually used) Resources of the Ontology: ");
		System.out.println("------------------------------------------ ");

		i = 0;

		while (itSt.hasNext()) { // show only different subjects and objects									
			st = itSt.nextStatement();
			resNext = st.getSubject(); // subject
			found = false;
			if (i > 0) {
				for (k = 0; k < i; k++) {
					if (res[k].equals(resNext))
						found = true;
				}
			}
			if (found == false) {
				res[i] = resNext;
				System.out.print("(" + (i + 1) + ") - ");
				System.out.println(resNext);
				i = i + 1;
			}
			resNext = st.getResource(); // object
			found = false;
			if (i > 0) {
				for (k = 0; k < i; k++) {
					if (res[k].equals(resNext))
						found = true;
				}
			}
			if (found == false) {
				res[i] = resNext;
				System.out.print("(" + (i + 1) + ") - ");
				System.out.println(resNext);
				i = i + 1;
			}
		}

		// chose Subject-Number
		correctInput = false;
		while (correctInput == false) {
			question = "Please enter the number of the resource you want to use as SUBJECT, or enter the SUBJECT as String: ";
			read = IoulHelps.readNum(question, 1, i, true);

			if (read.charAt(0)!= '?'){
				number = Integer.parseInt(read);
				// number entered
				im.subj = res[number - 1];
				System.out
						.print("Selected subject: " + im.subj.getLocalName());
				System.out.println();
				correctInput = true;
			} else {
				// String entered
				read = read.substring(1);
				im.subj = im.m.createResource(im.nsExample + read);
				System.out.println("Entered subject: " + im.subj.getLocalName());
				correctInput = true;
			}
		}

		// chose Object-Number
		correctInput = false;
		while (correctInput == false) {
			question = "Please enter the number of the resource you want to use as OBJECT, or enter the OBJECT as String: ";
			read = IoulHelps.readNum(question, 1,i,true);
			
			if (read.charAt(0)!= '?'){
				// number entered
				number = Integer.parseInt(read);
				im.obj = res[number - 1];
				System.out.print("Selected object: " + im.obj.getLocalName());
				System.out.println();
				correctInput = true;

			} else {
				// String entered
				read = read.substring(1);
				im.obj = im.m.createResource(im.nsExample + read);
				System.out.println("Entered object: " + im.obj.getLocalName());
				correctInput = true;
			}
		}
		executeAdd();		
		im.writeM(outFile);
	}
	/**
	 * executes add operation
	 
	 */
	public static void executeAdd() throws IOException {
		im.pattern = QueryFactory.create("SELECT ?x ?y where {?x <" + im.prop
				+ "> ?y}");// query pattern
		im.precond = new EmptyPrecondition();

		im.actions.add(new ApplyThisUpdate());
		im.actions.add(new FeedbackUpdate(im.subj.getLocalName() + " now "
				+ im.prop.getLocalName() + " " + im.obj.getLocalName()));
		im.actions.add(new FeedbackUpdate(
				"Therefore the following changes are necessary:"));
		tripleQueryAdd();

		// isAddHandler=true
		im.ch = new RDFChangeHandler(true, im.pattern, im.precond, im.actions, false);
		im.um.addChangeHandler(im.ch);

		// create graph describing the change
		Graph change = Factory.createDefaultGraph();
		Node n1 = NodeCreateUtils.create(im.subj.toString());
		Node n2 = NodeCreateUtils.create(im.prop.toString());
		Node n3 = NodeCreateUtils.create(im.obj.toString());
		change.add(Triple.create(n1, n2, n3));

		UpdateInsertData uid = new UpdateInsertData();
		uid.setData(change);
		System.out.println("-------------------------");
		System.out.println("Change:");
		System.out.println(uid.toString()); // ADD{....} statement

		OulUpdateAction.setManager(im.um);
		OulUpdateAction.execute(uid, im.gs);

		System.out.println();
		System.out.println("FEEDBACK:");
		System.out.println(OulUpdateAction.getFeedback()); 

		// new graph
		StmtIterator itSt = im.m.listStatements(); // counter of Statements

		System.out.println("Tripels of Graph after Change: ");
		System.out.println("------------------------------ ");

		itSt = im.m.listStatements();
		while (itSt.hasNext()) {
			System.out.println(itSt.nextStatement());
		}
	}

	/**
	 * all queries for the add part are listed in this method
	 */
	public static void tripleQueryAdd() throws IOException {
		Resource r, r1, r2;
		Property p;
		boolean correctInput;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String read = "";
		String question;
		int number = 0;
		char ans1, ans2;
		
		Resource[] types = im.getTypes();
		Property[] props = im.getProps();

		// is subject new Resource?
		r = im.checkResource(im.subj.toString());

		if (r == null) { // new Resource
			r = im.m.createResource(im.nsExample + im.subj.getLocalName()); // create resource
			question = im.subj.getLocalName()
						+ " is a new Resource. Please select the Type ( ";
			for (int i = 0; i < types.length-1; i++) {
				question = question+(i +1) + "-" + types[i].getLocalName()
							+ " ";
			}
			question = question +");";
			number = Integer.parseInt(IoulHelps.readNum(question, 1,types.length-1, false));

			im.m.add(r, RDF.type, types[number - 1]);
			im.actions.add(new FeedbackUpdate("   " + im.subj.getLocalName()
					+ " is a new resource of type "
					+ types[number - 1].getLocalName()));
		}

		// is object new Resource?
		r = im.checkResource(im.obj.toString());
		if (r == null) { // new Resource
			r = im.m.createResource(im.nsExample + im.obj.getLocalName()); // create resource
			question = im.obj.getLocalName()
						+ " is a new Resource. Please select the Type ( ";
			for (int i = 0; i < types.length; i++) {
				question = question+(i + 1) + "-" + types[i].getLocalName()+ " ";
			}
			question = question + "):";
			number = Integer.parseInt(IoulHelps.readNum(question,1,types.length,false));
			
			if (number != types.length){
				im.m.add(r, RDF.type, types[number - 1]);
				im.actions.add(new FeedbackUpdate("   " + im.obj.getLocalName()
						+ " is a new resource of type "
						+ types[number - 1].getLocalName()));
			}
		}

		// complete Affiliation data
		if (im.prop.getLocalName().equalsIgnoreCase("hasAffiliation")) {
			// Phonenumber
			p = im.m.createProperty(im.nsExample + "hasPhoneNumber");
			if (im.m.contains(im.subj, p) == false) {
				System.out.println("Please enter Phonenumber of "
						+ im.subj.toString() + ":");
				read = in.readLine();
				r = im.m.createResource(im.nsExample + read);
				im.m.add(im.subj, p, r);
				im.actions.add(new FeedbackUpdate("   add triple "
						+ im.subj.getLocalName() + " " + p.getLocalName() + " "
						+ r.getLocalName()));
			}
			// roomnumber
			p = im.m.createProperty(im.nsExample + "hasRoom");
			if (im.m.contains(im.subj, p) == false) {
				System.out.println("Please enter roomnumber of "
						+ im.subj.toString() + ":");
				read = in.readLine();
				r = im.m.createResource(im.nsExample + read);
				im.m.add(im.subj, p, r);
				im.actions.add(new FeedbackUpdate("   add triple "
						+ im.subj.getLocalName() + " " + p.getLocalName() + " "
						+ r.getLocalName()));
			}

			// working on projects ?
			System.out
					.println("Do you want to add projects on which "
							+ im.subj.getLocalName()
							+ " is working (only existing projects are allowed) (Y/N)?"); 							
			ans1 = IoulHelps.readchar();
			if (ans1 == 'Y') {
				correctInput = false;
				while (correctInput == false) {
					System.out
							.println("Enter project (end of input with empty String): ");
					read = in.readLine();
					if (read.compareTo("") == 0) {
						correctInput = true;
					} else {
						read = im.nsExample + read;
						r = im.checkResource(read);
						if (r != null) {
							r1 = im.checkTypeTriple(r);
							if (r1 != null) {
								if (im.checkTypeTriple(r).getLocalName()
										.equalsIgnoreCase("project")) {
									p = im.m.createProperty(im.nsExample + "worksOn");
									im.m.add(im.subj, p, r);
									im.actions.add(new FeedbackUpdate(
											"   add triple "
													+ im.subj.getLocalName() + " "
													+ p.getLocalName() + " "
													+ r.getLocalName()));
								}
							}
						}
					}
				}
			}
		}
		
		// is there a triple x leads y when Property is worksOn?
		if (im.prop.getLocalName().contains("worksOn")) {
			p = im.m.createProperty(im.nsExample + "leads");
			if (im.m.contains(im.subj, p, im.obj)) {
				System.out.println();
				System.out.println(im.subj.getLocalName().toString()
						+ " is already leader of "
						+ im.obj.getLocalName().toString());
				System.out.println("Do you realy want to add the triple --"
						+ im.subj.getLocalName() + " " + im.prop.getLocalName() + " "
						+ im.obj.getLocalName() + " (Y/N) ?");
				ans1 = IoulHelps.readchar();
				if (ans1 == 'N') {
					System.out
							.println("Exit program without any changes, because you don't want to add the chosen triple.");
					System.exit(0);
				}
			}
		}

		// is there a Triple x worksOn y when Proerty is leads?
		if (im.prop.getLocalName().contains("leads")) {
			p = im.m.createProperty(im.nsExample + "worksOn");
			if (im.m.contains(im.subj, p, im.obj)) {
				System.out.println();
				System.out.println("There is a triple --"
						+ im.subj.getLocalName().toString() + " worksOn "
						+ im.obj.getLocalName().toString());
				System.out
						.println("Do you want to REPLACE this triple by the triple --"
								+ im.subj.getLocalName()
								+ " "
								+ im.prop.getLocalName()
								+ " "
								+ im.obj.getLocalName() + "-- (Y/N) ?");
				ans1 = IoulHelps.readchar();
				if (ans1 == 'Y') {
					im.m.remove(im.subj, p, im.obj);
					im.actions.add(new FeedbackUpdate("   remove triple "
							+ im.subj.getLocalName() + " " + p.getLocalName()
							+ " " + im.obj.getLocalName()
							+ ", because worksOn was replaced with leads."));
				} else {
					System.out
							.println("Do you want to ADD the new triple (Y/N) ?");
					ans2 = IoulHelps.readchar();
					if (ans1 == 'N') {
						System.out
								.println("Exit program without changes, because you don't want to add the chosen triple.");
						System.exit(0);
					}
				}
			}
		}

		// if type of subject is phdStudent enter Theme of phdThesis
		r = im.checkResource(im.subj.toString());
		if (r != null) {
			r1 = im.checkTypeTriple(r);
			if (r1 != null) {
				if (r1.getLocalName().equalsIgnoreCase("phdStudent")) {
					p = im.m.createProperty(im.nsExample + "hasTheme");
					if (im.m.contains(im.subj, p) == false) {
						System.out.println("Enter Theme of phd Thesis of "
								+ im.subj.getLocalName() + " : ");
						read = in.readLine();
						r2 = im.m.createResource(im.nsExample + read);
						im.m.add(im.subj, p, r2);
						im.actions.add(new FeedbackUpdate("   add triple "
								+ im.subj.getLocalName() + " " + p.getLocalName()
								+ " " + r2.getLocalName()));
					}
				}
			}
		}

		// if type of subject or object is project, enter Budget and runtime
		r = im.checkResource(im.subj.toString());
		if (r != null) {
			r1 = im.checkTypeTriple(r);
			if (r1 != null) {
				if (r1.getLocalName().equalsIgnoreCase("project")) {
					p = im.m.createProperty(im.nsExample + "hasBudget");
					if (im.m.contains(im.subj, p) == false) {
						System.out.println("Enter Budget of project "
								+ im.subj.getLocalName() + " : ");
						read = in.readLine();
						r2 = im.m.createResource(im.nsExample + read);
						im.m.add(im.subj, p, r2);
						im.actions.add(new FeedbackUpdate("   add triple "
								+ im.subj.getLocalName() + " " + p.getLocalName()
								+ " " + read));
					}
					p = im.m.createProperty(im.nsExample + "hasRuntime");
					if (im.m.contains(im.subj, p) == false) {
						System.out.println("Enter Runtime of project "
								+ im.subj.getLocalName() + " : ");
						read = in.readLine();
						r2 = im.m.createResource(im.nsExample + read);
						im.m.add(im.subj, p, r2);
						im.actions.add(new FeedbackUpdate("   add triple "
								+ im.subj.getLocalName() + " " + p.getLocalName()
								+ " " + read));
					}
				}
			}
		}
		r = im.checkResource(im.obj.toString());
		if (r != null) {
			r1 = im.checkTypeTriple(r);
			if (r1 != null) {
				if (r1.getLocalName().equalsIgnoreCase("project")) {
					p = im.m.createProperty(im.nsExample + "hasBudget");
					if (im.m.contains(im.obj, p) == false) {
						System.out.println("Enter Budget of project "
								+ im.obj.getLocalName() + " : ");
						read = in.readLine();
						r2 = im.m.createResource(im.nsExample + read);
						im.m.add(im.obj, p, r2);
						im.actions.add(new FeedbackUpdate("   add triple "
								+ im.obj.getLocalName() + " " + p.getLocalName()
								+ " " + read));
					}
					p = im.m.createProperty(im.nsExample + "hasRuntime");
					if (im.m.contains(im.obj, p) == false) {
						System.out.println("Enter Runtime of project "
								+ im.obj.getLocalName() + " : ");
						read = in.readLine();
						r2 = im.m.createResource(im.nsExample + read);
						im.m.add(im.obj, p, r2);
						im.actions.add(new FeedbackUpdate("   add triple "
								+ im.obj.getLocalName() + " " + p.getLocalName()
								+ " " + read));
					}
				}
			}
		}
	}
}
