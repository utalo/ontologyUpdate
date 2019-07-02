/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package dev.optimizer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.engine.optimizer.Optimizer;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;

/**
 * Explain example
 *  
 * @author Markus Stocker
 */

public class query 
{
	private static String heuristic = null ;
	private static String inGraphFileName = null ;
	private static String inQueryFileName = null ;
	private static String inIndexFileName = null ;

	public static void main(String[] args) 
	{
		try
		{
			readCmdParams(args) ;
			Config config = new Config() ;
			
			config.setBasicPatternHeuristic(heuristic) ;
			
			Model data = FileManager.get().loadModel(inGraphFileName) ;
			
			if (inIndexFileName == null)
				Optimizer.enable(config) ;
			else
				Optimizer.enable(data, FileManager.get().loadModel(inIndexFileName), config) ;
			
			//Optimizer.disable() ;
			
			Query query = QueryFactory.read(inQueryFileName) ;
			QueryExecution qe = QueryExecutionFactory.create(query, data) ;
			ResultSet rs = qe.execSelect() ;
			ResultSetFormatter.out(rs) ;
			System.out.println("Result set size: " + rs.getRowNumber()) ;
		} catch (Exception e) { e.printStackTrace() ; }
	}
	
	// Read the command line params
	private static void readCmdParams(String[] args) throws Exception
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--graph"))
				inGraphFileName = args[i+1] ;
			else if (args[i].equals("--query"))
				inQueryFileName = args[i+1] ;
			else if (args[i].equals("--index"))
				inIndexFileName = args[i+1] ;
			else if (args[i].equals("--heuristic"))
				heuristic = args[i+1] ;
			else if (args[i].equals("--help"))
				usage() ;
		}

		if (inQueryFileName == null)
			usage() ;
	}
	
	// Print the usage of the main program
	private static void usage()
	{
		String usage = "query [options]\n" ;
		usage += "--graph [filename]\n" ;
		usage += "--query [filename]\n" ;
		usage += "--index [filename] (optional)\n" ;
		usage += "--heuristic [one of HeuristicsRegistry.] (optional)\n" ;
		
		System.out.println(usage) ;
		
		System.exit(0) ;
	}

}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */