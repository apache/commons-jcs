package org.apache.jcs.utils.threadpool.behavior;

/**
 * @author aaronsm
 *
 */
public interface IPoolConfiguration
{

  public static final String POLICY_ABORT = "ABORT";
  public static final String POLICY_BLOCK = "BLOCK";
  public static final String POLICY_RUN = "RUN";
  public static final String POLICY_WAIT = "WAIT";
  public static final String POLICY_DISCARDOLDEST = "DISCARDOLDEST";
  
  /**
   * @param useBoundary The useBoundary to set.
   */
  public abstract void setUseBoundary( boolean useBoundary );

  /**
   * @return Returns the useBoundary.
   */
  public abstract boolean isUseBoundary();

  /**
   * @param boundarySize
   *          The boundarySize to set.
   */
  public abstract void setBoundarySize( int boundarySize );

  /**
   * @return Returns the boundarySize.
   */
  public abstract int getBoundarySize();

  /**
   * @param maximumPoolSize
   *          The maximumPoolSize to set.
   */
  public abstract void setMaximumPoolSize( int maximumPoolSize );

  /**
   * @return Returns the maximumPoolSize.
   */
  public abstract int getMaximumPoolSize();

  /**
   * @param minimumPoolSize
   *          The minimumPoolSize to set.
   */
  public abstract void setMinimumPoolSize( int minimumPoolSize );

  /**
   * @return Returns the minimumPoolSize.
   */
  public abstract int getMinimumPoolSize();

  /**
   * @param keepAliveTime
   *          The keepAliveTime to set.
   */
  public abstract void setKeepAliveTime( int keepAliveTime );

  /**
   * @return Returns the keepAliveTime.
   */
  public abstract int getKeepAliveTime();

  /**
   * should be ABORT, BLOCK, RUN, WAIT, DISCARDOLDEST.
   * 
   * If an incorrect value is returned, run will be used.
   * 
   * @param whenBlockedPolicy The whenBlockedPolicy to set.
   */
  public abstract void setWhenBlockedPolicy( String whenBlockedPolicy );

  /**
   * @return Returns the whenBlockedPolicy.
   */
  public abstract String getWhenBlockedPolicy();

  /**
   * @param startUpSize
   *          The startUpSize to set.
   */
  public abstract void setStartUpSize( int startUpSize );

  /**
   * @return Returns the startUpSize.
   */
  public abstract int getStartUpSize();
}