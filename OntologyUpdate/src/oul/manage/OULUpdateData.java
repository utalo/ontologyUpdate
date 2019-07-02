package oul.manage;

import com.hp.hpl.jena.graph.Graph;

/**
 * Class for maintaining the data to be added and deleted from a graph during an OUL-supported update
 * @author uhe
 *
 */
public class OULUpdateData {

	Graph addData = null;
	Graph deleteData = null;

	public OULUpdateData(){ /*empty*/	}
	
	public void setAddData( Graph addData ){
		this.addData = addData;
	}
	
	public void setDeleteData( Graph deleteData ){
		this.deleteData = deleteData;
	}

	public Graph getAddData() {
		return addData;
	}

	public Graph getDeleteData() {
		return deleteData;
	}
}
