package org.apache.jcs.utils.threadpool;

/**
 * This object holds configuration data for a thread pool.
 * 
 * @author Aaron Smuts
 *  
 */
public class PoolConfiguration implements Cloneable
{
  private int     boundarySize     = 75;

  private int     maximumPoolSize  = 150;

  private int     minimumPoolSize  = 4;

  private int     keepAliveTime    = 1000 * 60 * 5;

  private boolean abortWhenBlocked = false;

  private int     startUpSize      = 4;

  /**
   * Default
   *  
   */
  public PoolConfiguration()
  {
    // nop
  }

  /**
   * 
   * @param boundarySize
   * @param maximumPoolSize
   * @param minimumPoolSize
   * @param keepAliveTime
   * @param abortWhenlocked
   * @param startUpSize
   */
  public PoolConfiguration(int boundarySize, int maximumPoolSize,
      int minimumPoolSize, int keepAliveTime, boolean abortWhenBlocked,
      int startUpSize)
  {
  }

  /**
   * @param boundarySize
   *          The boundarySize to set.
   */
  public void setBoundarySize( int boundarySize )
  {
    this.boundarySize = boundarySize;
  }

  /**
   * @return Returns the boundarySize.
   */
  public int getBoundarySize()
  {
    return boundarySize;
  }

  /**
   * @param maximumPoolSize
   *          The maximumPoolSize to set.
   */
  public void setMaximumPoolSize( int maximumPoolSize )
  {
    this.maximumPoolSize = maximumPoolSize;
  }

  /**
   * @return Returns the maximumPoolSize.
   */
  public int getMaximumPoolSize()
  {
    return maximumPoolSize;
  }

  /**
   * @param minimumPoolSize
   *          The minimumPoolSize to set.
   */
  public void setMinimumPoolSize( int minimumPoolSize )
  {
    this.minimumPoolSize = minimumPoolSize;
  }

  /**
   * @return Returns the minimumPoolSize.
   */
  public int getMinimumPoolSize()
  {
    return minimumPoolSize;
  }

  /**
   * @param keepAliveTime
   *          The keepAliveTime to set.
   */
  public void setKeepAliveTime( int keepAliveTime )
  {
    this.keepAliveTime = keepAliveTime;
  }

  /**
   * @return Returns the keepAliveTime.
   */
  public int getKeepAliveTime()
  {
    return keepAliveTime;
  }

  /**
   * @param abortWhenBlocked
   *          The abortWhenBlocked to set.
   */
  public void setAbortWhenBlocked( boolean abortWhenBlocked )
  {
    this.abortWhenBlocked = abortWhenBlocked;
  }

  /**
   * @return Returns the abortWhenBlocked.
   */
  public boolean isAbortWhenBlocked()
  {
    return abortWhenBlocked;
  }

  /**
   * @param startUpSize
   *          The startUpSize to set.
   */
  public void setStartUpSize( int startUpSize )
  {
    this.startUpSize = startUpSize;
  }

  /**
   * @return Returns the startUpSize.
   */
  public int getStartUpSize()
  {
    return startUpSize;
  }

  /**
   * To string for debugging purposes.
   */
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append( "boundarySize = [" + boundarySize + "]" );
    buf.append( "maximumPoolSize = [" + maximumPoolSize + "]" );
    buf.append( "minimumPoolSize = [" + minimumPoolSize + "]" );
    buf.append( "keepAliveTime = [" + keepAliveTime + "]" );
    buf.append( "abortWhenBlocked = [" + abortWhenBlocked + "]" );
    buf.append( "startUpSize = [" + startUpSize + "]" );
    return buf.toString();
  }

  /**
   * Copies the instance variables to another instance.
   */
  public Object clone()
  {
    return new PoolConfiguration( boundarySize, maximumPoolSize,
        minimumPoolSize, keepAliveTime, abortWhenBlocked, startUpSize );
  }
}