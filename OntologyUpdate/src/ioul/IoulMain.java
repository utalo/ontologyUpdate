package ioul;

import ioul.AddTriple;
import ioul.DelTriple;
import ioul.IoulHelps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

public class IoulMain {

	public static void main(String[] args) throws IOException {
			
		String inFile = "src/ioul/model_in.rdf";
		String outFile = "src/ioul/model_out.rdf";
		int number=0;
		String question,read;
		
		read = " ";
		// create model and read RDF-triples from Inputfile c:\temp\model_in.rdf
		boolean reachedEnd = false;
		IoulModel im = new IoulModel(inFile);
		// Save triples in outputfile c:\temp\model_out.rdf
		im.writeM(outFile);
		
		while (reachedEnd == false){
			// create new ioul-model
			im = new IoulModel(outFile);
			
			//  choose between add and delete			 
			question = "Do you want to delete (1) or add (2) a tripel?";
			number = Integer.parseInt(IoulHelps.readNum(question, 1, 2,false));
			if (number == 1){
				DelTriple.prepareDel(im, outFile);
			} else {
				AddTriple.prepareAdd(im, outFile);
			}
			
			// continue program (second run)
			System.out.println();
			System.out.println("Do you want to continue and add/delete another triple?[Y/N]");
			if (IoulHelps.readchar() == 'N') reachedEnd = true;			
		}		
	}
}