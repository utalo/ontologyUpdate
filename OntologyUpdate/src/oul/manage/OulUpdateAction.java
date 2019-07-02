package oul.manage;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.modify.op.Update;
import com.hp.hpl.jena.sparql.modify.op.UpdateClear;
import com.hp.hpl.jena.sparql.modify.op.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.op.UpdateDelete;
import com.hp.hpl.jena.sparql.modify.op.UpdateDeleteData;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsert;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsertData;
import com.hp.hpl.jena.sparql.modify.op.UpdateModify;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Extension of the class UpdateAction (processing of Sparql/Update requests)
 * Responsible for the execution of UpdateActions within the OUL framework
 * @author uhe
 *
 */
public class OulUpdateAction extends UpdateAction {

	private static UpdateManager manager = new UpdateManager();
	private static IndentedWriter out = IndentedWriter.stderr;
	
	private static String feedback = "";
	
	private OulUpdateAction() {}
	
	public static void setManager( UpdateManager m ){
		manager = m;
	}
	
	public static void execute(UpdateRequest request, Graph graph)
    {
        execute(request, GraphStoreFactory.create(graph), null) ; 
    }

    /** Execute SPARQL/Update operations.
     * @param request
     * @param graphStore
     */
    public static void execute(UpdateRequest request, GraphStore graphStore)
    {
        execute(request, graphStore, null) ;
    }

    /** Execute SPARQL/Update operations.
     * @param request
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void execute(UpdateRequest request, Model model, QuerySolution initialBinding)
    {
        execute(request, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Execute SPARQL/Update operations.
     * @param request
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void execute(UpdateRequest request, Dataset dataset, QuerySolution initialBinding)
    {
        execute(request, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Execute SPARQL/Update operations.
     * @param request
     * @param graph
     * @param binding Presets for variables.
     */
    public static void execute(UpdateRequest request, Graph graph, Binding binding)
    {
        execute(request, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Execute SPARQL/Update operations within OUL.
     * @param request 
     * @param graphStore
     * @param binding Presets for variables.
     */

	public static void execute(UpdateRequest request, GraphStore graphStore, Binding binding)
    {
    	feedback = "";
		UpdateDataVisitor updateDataVis = new UpdateDataVisitor();
		ComplexUpdate cu = new ComplexUpdate();
    	if( request.getUpdates().size() > 1 ){
			System.err.println("Only one update action per request allowed");
    		//throw new OULException();
		}
    	UpdateRequest newReq = new UpdateRequest();
		//iterate over the updates of the request (is only one for now)
		for( Iterator iter = request.getUpdates().iterator() ; iter.hasNext() ; ){
			Update u = (Update)iter.next();
			OULUpdateData data = null;
			// get the data that should be added and deleted
			if( u instanceof UpdateDeleteData ){
				data = updateDataVis.getData( (UpdateDeleteData)u ) ;
			} else if ( u instanceof UpdateInsertData){
				data = updateDataVis.getData( (UpdateInsertData)u );
			} else if ( u instanceof UpdateDelete ){
				data = updateDataVis.getData( (UpdateDelete)u );
			} else if ( u instanceof UpdateInsert ){
				data = updateDataVis.getData( (UpdateInsert)u );
			} else if ( u instanceof UpdateModify ){
				data = updateDataVis.getData( (UpdateModify)u );
			} else if ( u instanceof UpdateClear ){
				data = updateDataVis.getData( (UpdateClear)u );
			} else if ( u instanceof UpdateCreate ){
				data = updateDataVis.getData( (UpdateCreate)u );
			}
			if( data == null ){
				newReq.addUpdate(u);
			} else { //it is a modify operation i.e. one that changes data in the KB
				if( data.getDeleteData() != null ){
					//find a suitable update manager for the delete part
					//get data to be deleted additionally or added
					cu = manager.findUpdates(false,data.getDeleteData()); 
					List ul = cu.getUpdates();
					Iterator iter2 = ul.iterator();
					while(iter2.hasNext()){
						newReq.addUpdate((Update)iter2.next());
					}
					binding = combineBindings( binding, cu.getBinding() );
				}
				if( data.getAddData() != null ){
					//find a suitable update manager for the add part
					//get data to be added additionally or deleted
					cu = manager.findUpdates(true, data.getAddData()); 
					List ul = cu.getUpdates();
					for( Iterator ulIter = ul.iterator(); ulIter.hasNext() ; ){
						Update update = (Update)ulIter.next(); 
						newReq.addUpdate(update);
					}
					binding = combineBindings( binding, cu.getBinding() );
				}
			}
		}
		//execute all required actions
        UpdateProcessor uProc = UpdateFactory.create(newReq, graphStore, binding) ;
        uProc.execute() ;
        feedback = uProc.getFeedback();
    }

	/** Execute a single SPARQL/Update operation.
     * @param update
     * @param model
     */
    public static void execute(Update update, Model model)
    { 
    	execute(update, model, null) ; 
    }
    
    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param dataset
     */
    public static void execute(Update update, Dataset dataset)
    {
        execute(update, dataset, null) ; 
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param graph
     */
    public static void execute(Update update, Graph graph)
    {
        execute(update, GraphStoreFactory.create(graph), null) ; 
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param graphStore
     */
    public static void execute(Update update, GraphStore graphStore)
    {
        execute(update, graphStore, null) ;
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param model
     * @param initialBinding Presets for variables.
     */
    public static void execute(Update update, Model model, QuerySolution initialBinding)
    {
        execute(update, GraphStoreFactory.create(model), BindingUtils.asBinding(initialBinding)) ; 
    }
    
    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param dataset
     * @param initialBinding Presets for variables.
     */
    public static void execute(Update update, Dataset dataset, QuerySolution initialBinding)
    {
        execute(update, GraphStoreFactory.create(dataset), BindingUtils.asBinding(initialBinding)) ; 
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param graph
     * @param binding Presets for variables.
     */
    public static void execute(Update update, Graph graph, Binding binding)
    {
        execute(update, GraphStoreFactory.create(graph), binding) ; 
    }

    /** Execute a single SPARQL/Update operation.
     * @param update
     * @param graphStore
     * @param binding Presets for variables.
     */
    public static void execute(Update update, GraphStore graphStore, Binding binding)
    {
    	UpdateRequest ur = new UpdateRequest();
    	ur.addUpdate(update);
    	execute(ur, graphStore , binding);
    }

    public static void setIntendedWriter( IndentedWriter outNew ){
    	out = outNew;
    }
    
    /**
     * Get the output of processing a SPARQL/Update
     * @return
     */
    public static String getFeedback(){
    	return feedback;
    }
    
    /**
     * Merge two variable bindings into one
     * @param b1
     * @param b2
     * @return
     */
    private static Binding combineBindings(Binding b1,
			Binding b2) {
    	if( b1 == null ){
    		return b2;
    	}
    	if( b2 == null ){
    		return b1;
    	}
		Iterator vars = b2.vars();
		while( vars.hasNext() ){
			Var v = (Var)vars.next();
			b1.add( v , b2.get(v));
			
		}
		return b1;
		
	}
}
