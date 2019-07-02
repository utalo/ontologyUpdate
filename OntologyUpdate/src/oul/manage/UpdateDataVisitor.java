package oul.manage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oul.update.FeedbackUpdate;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.modify.op.Update;
import com.hp.hpl.jena.sparql.modify.op.UpdateClear;
import com.hp.hpl.jena.sparql.modify.op.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.op.UpdateDelete;
import com.hp.hpl.jena.sparql.modify.op.UpdateDeleteData;
import com.hp.hpl.jena.sparql.modify.op.UpdateDrop;
import com.hp.hpl.jena.sparql.modify.op.UpdateExt;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsert;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsertData;
import com.hp.hpl.jena.sparql.modify.op.UpdateLoad;
import com.hp.hpl.jena.sparql.modify.op.UpdateModify;
import com.hp.hpl.jena.sparql.modify.op.UpdateModifyBase;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.update.GraphStore;

public class UpdateDataVisitor {

	private GraphStore graphStore;
	private Binding binding;
	
	public OULUpdateData getData(Update u) {
		return null;
	}
	
	public OULUpdateData getData(UpdateInsertData insertData) {
		OULUpdateData ud = new OULUpdateData();
		ud.setAddData(insertData.getData());
		return ud;
	}
	
	public OULUpdateData getData(UpdateDeleteData deleteData) {
		OULUpdateData ud = new OULUpdateData();
		ud.setDeleteData(deleteData.getData());
		return ud;
	}
	
	public OULUpdateData getData(UpdateInsert insert) {
		return getDataModify(insert);
	}
	
	public OULUpdateData getData(UpdateDelete delete) {
		return getDataModify(delete);
	}
	
	public OULUpdateData getData(UpdateModify modify) {
		return getDataModify(modify);
	}
	
	public OULUpdateData getData(UpdateClear clear){
		return null;
	}

	public OULUpdateData getData( UpdateLoad load ){
		return null;
	}
	
	public OULUpdateData getData(UpdateDrop drop){
		return null;
    }

    public OULUpdateData getData(UpdateCreate create){
    	return null;
    }

    public OULUpdateData getData(UpdateExt updateExt){
    	return null;
    }

    public OULUpdateData getData( FeedbackUpdate u ){
    	return null;
    }
    
	private OULUpdateData getDataModify(UpdateModifyBase modify) {
		final List bindings = evalBindings(modify.getElement() ) ;
		OULUpdateData ud = new OULUpdateData();
		if ( modify.getDeletes() != null ){
            QueryIterator qIter = new QueryIterPlainWrapper(bindings.iterator()) ;
            Collection acc = subst(modify.getDeletes(), qIter) ;
            Graph graph = Factory.createDefaultGraph();
            for( Iterator iter =acc.iterator() ; iter.hasNext() ; ){
            	Triple t = (Triple)iter.next();
            	graph.add(t);
            }
            ud.setDeleteData(graph);
        }
		if ( modify.getInserts() != null ){
            QueryIterator qIter = new QueryIterPlainWrapper(bindings.iterator()) ;
            Collection acc = subst(modify.getDeletes(), qIter) ;
            Graph graph = Factory.createDefaultGraph();
            for( Iterator iter =acc.iterator() ; iter.hasNext() ; ){
            	Triple t = (Triple)iter.next();
            	graph.add(t);
            }
            ud.setAddData(graph);
        }
		return ud;
	}
	
    private List evalBindings(Element pattern)
    {
        List bindings = new ArrayList() ;
        if ( pattern != null )
        {
            Plan plan = QueryExecutionFactory.createPlan(pattern, graphStore, binding) ;
            QueryIterator qIter = plan.iterator() ;

            for( ; qIter.hasNext() ; )
            {
                Binding b = qIter.nextBinding() ;
                bindings.add(b) ;
            }
            qIter.close() ;
        }
        else
            bindings.add(BindingRoot.create()) ;
        return bindings ;
    }
    
    private static Collection subst(Template template, QueryIterator qIter)
    {
        Set acc = new HashSet() ;
        for ( ; qIter.hasNext() ; )
        {
            Map bNodeMap = new HashMap() ;
            Binding b = qIter.nextBinding() ;
            template.subst(acc, bNodeMap, b) ;
        }

        for ( Iterator iter = acc.iterator() ; iter.hasNext() ; )
        {
            Triple triple = (Triple)iter.next() ;
            if ( ! isGroundTriple(triple))
            {
                ALog.warn(UpdateDataVisitor.class, "Unbound triple: "+FmtUtils.stringForTriple(triple)) ;
                iter.remove() ;
            }
        }
        
        return acc ;
    }
    
    private static boolean isGroundTriple(Triple triple)
    {
        return 
            isGroundNode(triple.getSubject()) &&
            isGroundNode(triple.getPredicate()) &&
            isGroundNode(triple.getObject()) ;
    }
    
    private static boolean isGroundNode(Node node)
    {
        return node.isConcrete() ; 
    }
}


