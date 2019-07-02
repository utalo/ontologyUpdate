package oul.manage;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.op.Update;

/**
 * A complex update is an update which consists of a list of atomic updates
 * This is needed e.g. for loop bodies
 * @author uhe
 *
 */
public class ComplexUpdate {

	private List updates;
	Binding b;
	
	public ComplexUpdate(){
		updates = new LinkedList();
		b = null;
	}
	
	public void addUpdate( Update u ){
		updates.add(u);
	}
	
	public void setBinding(Binding b){
		this.b = b;
	}
	
	public List getUpdates(){
		return updates;
	}
	
	public Binding getBinding(){
		return b;
	}
}
