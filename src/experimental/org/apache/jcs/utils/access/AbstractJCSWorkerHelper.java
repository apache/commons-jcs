/*
 * Created on Aug 8, 2004
 *
 */
package org.apache.jcs.utils.access;


/**
 * @author tsavo
 *
 */
public abstract class AbstractJCSWorkerHelper implements JCSWorkerHelper {
	private boolean finished = false;
	/**
	 * 
	 */
	public AbstractJCSWorkerHelper() {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean isFinished(){
		return finished;
	}
	
	public void setFinished(boolean isFinished){
		finished = isFinished;
	}
}
