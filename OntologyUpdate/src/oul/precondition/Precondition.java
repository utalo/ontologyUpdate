package oul.precondition;

import java.util.ArrayList;
import java.util.List;

import oul.manage.ChangeHandler;
import oul.manage.OULException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.update.GraphStore;

public abstract class Precondition {
	public static final int SYNTACTIC = 1;
	public static final int SEMANTIC = 2;
	public static final int SEMANTICCHANGED = 3;
	public static final int OR = 4;
	public static final int AND = 5;
	
	protected int type;
	protected ChangeHandler changehandler;
	
	protected Precondition(){/*empty */}
	
	protected Precondition( int type ){
		this.type = type;
	}
	
	protected Precondition( ChangeHandler ch ){
		changehandler = ch;
	}
	
	protected Precondition( int type , ChangeHandler ch ){
		this.type = type;
		changehandler = ch;
	}
	
	public void setChangeHandler( ChangeHandler ch ){
		changehandler = ch;
	}
	
	public int getType(){
		return type;
	}
	
	public abstract List evaluateCondition( Model model , Graph change , QuerySolution qs ) throws OULException;
	
	protected List evalBindings(Query q , Model m , QuerySolution qs ){
        List bindings = new ArrayList() ;
        if ( q != null )
        {
            QueryExecution qe = QueryExecutionFactory.create(q, m);
            if( qs != null ){
            	qe.setInitialBinding( qs );
            }
            ResultSet rs = qe.execSelect();
            
            for( ; rs.hasNext() ; ){
                Binding binding = rs.nextBinding() ;
                bindings.add(binding) ;
            }
        }
        else
            bindings.add(BindingRoot.create()) ;
        return bindings ;
    }
	
	
	
}
