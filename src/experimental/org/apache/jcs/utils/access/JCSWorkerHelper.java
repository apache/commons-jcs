/*
 * Created on Aug 8, 2004
 *
 */
package org.apache.jcs.utils.access;

/**
 * Interface for doing a piece of work which is expected to be cached.
 * This is ment to be used in conjunction with JCSWorker.
 * Implement doWork() to return the work being done. isFinished() should return false
 * until setFinished(true) is called, after which time it should return true. 
 * 
 * @author tsavo
 */
public interface JCSWorkerHelper {
	/**
	 * Tells us weather or not the work has been completed.
	 * This will be called automatically by JCSWorker. You should not call it yourself.
	 * @return True if the work has allready been done, otherwise false.
	 */
	public boolean isFinished();
	/**
	 * Sets weather or not the work has been done.
	 * @param isFinished True if the work has allready been done, otherwise false.
	 */
	public void setFinished(boolean isFinished);
	/**
	 * The method to implement to do the work that should be cached.
	 * JCSWorker will call this itself! You should not call this directly.
	 * @return The result of doing the work to be cached.
	 * @throws Exception If anything goes wrong while doing the work, an Exception should be thrown.
	 */
	public Object doWork() throws Exception;
}
