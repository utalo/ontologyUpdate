package examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import com.hp.hpl.jena.rdf.model.*;
/**
 * 
 * @author Victoria
 *
 */
public class ExampleMain {
	
	static String nsAifb = "http://aifb/";
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	static String read="";
	
	public static void main(String[] args) throws IOException {//choose add or delete 
		char ans1;
		int number = 3;
		while (number != 1 & number != 2){
			System.out.println("Do you want to delete (1) or add (2) a tripel?");
			read = in.readLine();
			try{
				number = Integer.parseInt(read);
			} catch (NumberFormatException ex) {
				number=3;
			}
		}
		if (number == 1) {
			delTripel();
		} else {
			addTripel();
		}
	}	

	public static void delTripel() throws IOException {
		int number = 0;
		boolean correctInput;

		// create ontology
		ExampleX exX = new ExampleX(); 
		
		int i = 1;
		StmtIterator itSt= exX.m.listStatements();  // counter of Statements
		Statement st;
		
		// list the triples
		System.out.println();
		System.out.println("Triples of the Ontology: ");
		System.out.println("------------------------ ");

		while (itSt.hasNext()){			// list triples with numbers
			System.out.print("(" + i + ") - ");
			st = itSt.nextStatement();
			System.out.println(st);
			i = i + 1;
		}
	
		correctInput = false;
		while(correctInput == false){
			System.out.println();
			System.out.println("Please enter the number of the TRIPLE you" +
					" want to delete, or enter the TRIPLE as String subj prop obj: ");	
			read = in.readLine();
			try {
				// Number entered
				number = Integer.parseInt(read);
				if (number <= i & number > 0) {		
					itSt = exX.m.listStatements();
					int j=0;
					while(j<number){
						st = itSt.nextStatement();
						exX.subj = st.getSubject();
						exX.prop = st.getPredicate();
						exX.obj = st.getResource();
						j=j+1;
					}
					correctInput = true;
					System.out.print("You have selected the Triple: ");
					System.out.println(exX.subj.getLocalName()+"  "+exX.prop.getLocalName()+"  "+exX.obj.getLocalName());
					System.out.println();
					exX.executeDel();
				} else {
					System.out.println("Wrong number, try again.");
				}
			}
		    catch (NumberFormatException ex) 
		    { 
		    	// Triple entered
				//Splits the entered triple 
				String triple[]=read.split(" ");	// blank as seperator in the string for the 3 values 
				
				if (triple.length!=3){			// there has to be 3 words
					System.out.println("A triple has to consists of exactly 3 parameters.");
				}else{
					boolean found=false; 
					System.out.println("Subject: "+triple[0]+"\n"+"Property: "+triple[1]+"\n"+"Object: "+triple[2]);
					itSt = exX.m.listStatements();
					while (itSt.hasNext() && found==false){			
						st = itSt.nextStatement();
						exX.subj = st.getSubject();
						exX.prop = st.getPredicate();
						exX.obj = st.getResource();
					
						if((nsAifb+triple[0]).equals(exX.subj.toString())==true && (nsAifb+triple[1]).equals(exX.prop.toString())==true && (nsAifb+triple[2]).equals(exX.obj.toString())==true){
							found=true;
							correctInput=true;
							System.out.println("Triple was found in the ontology!");
							exX.executeDel();
						}
					}				
				}
		    } 		
		}
	}	
	
	public static void addTripel() throws IOException {
		
		Property props [];
		Resource res [];
		Property propNext;
		Resource resNext;
		int i;
		int k;
		boolean correctInput;
		int number;
		
		// create ontology
		ExampleX exX = new ExampleX(); 

		// choose Property 
		props = new Property [(int)exX.m.size()];
		
		System.out.println();
		System.out.println("Properties of the Ontology: ");
		System.out.println("--------------------------- ");
		
		boolean found ;
		i = 0;
		StmtIterator itSt = exX.m.listStatements();
		Statement st;

		while (itSt.hasNext()){		// show only different properties, not all
			found = false;
			st = itSt.nextStatement();
			propNext = st.getPredicate();
			if (i>0){
				for (k=0;k<i;k++){
					if (props[k].equals(propNext)) found=true;
				}
			} 
			if (found == false){
				props[i] = propNext;
				System.out.print("(" + (i+1) + ") - ");
				System.out.println(propNext); 
				i = i + 1;
			}	
		}
		
		// choose Property-Number
		correctInput = false;
		while(correctInput == false){
			System.out.println();
			System.out.println("Please enter the number of the PPROPERTY you want to work with or enter PROPRTY as String: ");

			read = in.readLine();
			try {
				// Number entered
				number = Integer.parseInt(read);
				if (number <= i & number>0) {		
					exX.prop = props[number-1];
					System.out.print("Selected property: "+ exX.prop.getLocalName());
					System.out.println();
					correctInput = true;
				} else {
					System.out.println("Wrong number, try again.");
				}				
			}
		    catch (NumberFormatException ex) 
		    { 
		    	// String entered
		    	found = false;
				itSt = exX.m.listStatements();
				while (itSt.hasNext() && found==false){			
					// checks, if entered property exists in the ontology
					st = itSt.nextStatement();
				
					if((nsAifb+read).equals(st.getPredicate().toString())==true ){
						found=true;
						correctInput=true;
						System.out.println("Property was found in the ontology!");
					}
				}		
				if (found == false){
					System.out.println("The entered Property isn't valid, you can only enter an existingproperty. Try again.");
				}
		    }

		}
	
		// choose/Enter subject and object
		itSt= exX.m.listStatements();   
		res = new Resource [2*(int)exX.m.size()];
		
		System.out.println();
		System.out.println("Resources of the Ontology: ");
		System.out.println("------------------------- ");
		
		i = 0;
		
		while (itSt.hasNext()){		// show only different subjects and objects, not all	
			st = itSt.nextStatement();
			resNext = st.getSubject();		//subject
			found = false;
			if (i>0){
				for (k=0;k<i;k++){
					if (res[k].equals(resNext)) found=true;
				}
			} 
			if (found == false){
				res[i] = resNext;
				System.out.print("(" + (i+1) + ") - ");
				System.out.println(resNext); 
				i = i + 1;
			}	
			resNext = st.getResource();		// object
			found = false;
			if (i>0){
				for (k=0;k<i;k++){
					if (res[k].equals(resNext)) found=true;
				}
			} 
			if (found == false){
				res[i] = resNext;
				System.out.print("(" + (i+1) + ") - ");
				System.out.println(resNext); 
				i = i + 1;
			}	
		}
		
		// choose Subject-Number
		correctInput = false;
		while(correctInput == false){
			System.out.println();
			System.out.println("Please enter the number of the resource you want take as SUBJECT, or enter the SUBJECT as String: ");
	
			read = in.readLine();
			try { 
		      number = Integer.parseInt(read); 
		      // number entered
				if (number <= i & number > 0) {		
					exX.subj = res[number-1];
					System.out.print("Selected subject: "+ exX.subj.getLocalName());
					System.out.println();
					correctInput = true;
				} else {
					System.out.println("Wrong number, try again.");
				}
			} 
		    catch (NumberFormatException ex) 
		    { 
		    	//String entered	
				exX.subj= exX.m.createResource(nsAifb +read);
				System.out.println("Entered subject: "+ exX.subj.getLocalName());
				correctInput = true;

		    } 		
		}		
			
		// choose Object-Number
		correctInput = false;
		while(correctInput == false){
			System.out.println();
			System.out.println("Please enter the number of the resource you want to take for OBJECT, or enter the OBJECT as String: ");
	
			read = in.readLine();
			try {
				number = Integer.parseInt(read);
				if (number <= i & number > 0) {		
					exX.obj = res[number-1];
					System.out.print("Selected object: "+ exX.obj.getLocalName());
					System.out.println();
					correctInput = true;
					exX.executeAdd();
				} else {
					System.out.println("Wrong number, try again.");
				}
			}
		    catch (NumberFormatException ex) 
		    { 
		    	//String entered	
				exX.obj= exX.m.createResource(nsAifb +read);
				System.out.println("Entered object: "+ exX.obj.getLocalName());
				correctInput = true;
		    } 	
		}	
		
	 	exX.executeAdd(); 
	}
}	