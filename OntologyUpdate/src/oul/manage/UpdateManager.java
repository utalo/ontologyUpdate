package oul.manage;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oul.update.ApplyThisUpdate;
import oul.update.LoopUpdate;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.lang.arq.ARQParser;
import com.hp.hpl.jena.sparql.lang.arq.ParseException;
import com.hp.hpl.jena.sparql.modify.op.Update;
import com.hp.hpl.jena.sparql.modify.op.UpdateDeleteData;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsertData;
import com.hp.hpl.jena.util.FileManager;

/**
 * UpdateManager maintains the knowledge base and a list of changehandlers defined for it
 * @author uhe
 *
 */
public class UpdateManager {
	private List addHandlers;
	private List deleteHandlers;
	private Model model;
	
	public UpdateManager(){
		addHandlers = new LinkedList();
		deleteHandlers = new LinkedList();
		model = ModelFactory.createDefaultModel();
	}
	
	public UpdateManager( Graph g ){
		addHandlers = new LinkedList();
		deleteHandlers = new LinkedList();
		model = ModelFactory.createModelForGraph(g);
		
	}
	
	public void addChangeHandler( ChangeHandler ch ){
		if( ch.isAddHandler() ){
			addHandlers.add(ch);
		} else {
			deleteHandlers.add(ch);
		}
	}
	
	/**
	 * Reader and parser for changehandlers
	 * @param inputFileName
	 * @throws IllegalArgumentException
	 */
	public void readChangeHandler( String inputFileName ) throws IllegalArgumentException{
		InputStream in = FileManager.get().open( inputFileName );
		if( in == null ){
			throw new IllegalArgumentException("File " + inputFileName + " not found!");
		}
		ARQParser p = new ARQParser( in );
		try {
			this.addChangeHandler( p.ChangeHandler() );
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * find additional updates for a desired change
	 * @param isAdd
	 * @param change
	 * @return
	 */
	public ComplexUpdate findUpdates( boolean isAdd , Graph change ){
		ComplexUpdate cu = new ComplexUpdate();
		Iterator chIter;
		if( isAdd ){
			chIter = addHandlers.iterator();
		} else {
			chIter = deleteHandlers.iterator();
		}
		ChangeHandler currChangeHandler;
		while( chIter.hasNext() ){
			currChangeHandler = (ChangeHandler)chIter.next();
			if( currChangeHandler.matches(change) 
					&& currChangeHandler.preconditionFulfilled( model , change)){
				List updates = currChangeHandler.getUpdates( change );
				cu.setBinding(currChangeHandler.getBinding());
				for( Iterator iter = updates.iterator() ; iter.hasNext() ; ){
					Update u = (Update)iter.next();
					if( u instanceof ApplyThisUpdate ) {
						if( isAdd ){
							UpdateInsertData updateData = new UpdateInsertData();
							updateData.setData(change);
							cu.addUpdate(updateData);
						} else {
							UpdateDeleteData updateData = new UpdateDeleteData();
							updateData.setData(change);
							cu.addUpdate(updateData);
						}
					} else if ( u instanceof LoopUpdate ){
						QuerySolution qs = null;
						if( cu.getBinding() != null ){
							qs = new ResultBinding( model , cu.getBinding() );
						}
						List updateList = ((LoopUpdate)u).getUpdates( model, change , qs );
						Iterator updateListIter = updateList.iterator();
						while( updateListIter.hasNext() ){
							cu.addUpdate( (Update)updateListIter.next() );
						}
					} else {
						cu.addUpdate(u);
					}
				}
			}
		}
		return cu;
	}

}
