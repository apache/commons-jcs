package org.apache.jcs.auxiliary.disk.jdbc.mysql;

import org.apache.jcs.auxiliary.disk.jdbc.JDBCDiskCacheAttributes;

/**
 * This has additional attributes that are particular to the MySQL disk cache.
 * <p>
 * @author Aaron Smuts
 */
public class MySQLDiskCacheAttributes
    extends JDBCDiskCacheAttributes
{
    private static final long serialVersionUID = -6535808344813320061L;    
    
    /**
     * For now this is a simpel comma delimited list of HH:MM times to optimize
     * the table. If none is supplied, then no optimizations will be performed.
     * <p>
     * In the future we can add a chron like scheduling system. This is to meet
     * a pressing current need.
     * <p>
     * 03:01,15:00 will cause the optimizer to run at 3 am and at 3 pm.
     */
    private String optimizationSchedule = null;
    
    /**
     * If true, we will balk, that is return null during optimization rather than block.
     */
    public static final boolean DEFAULT_BALK_DURING_OPTIMIZATION = true;
    
    /**
     * If true, we will balk, that is return null during optimization rather than block.
     * <p>
     * <a href="http://en.wikipedia.org/wiki/Balking_pattern">Balking</a>
     */
    private boolean balkDuringOptimization = DEFAULT_BALK_DURING_OPTIMIZATION;

    /**
     * @param optimizationSchedule The optimizationSchedule to set.
     */
    public void setOptimizationSchedule( String optimizationSchedule )
    {
        this.optimizationSchedule = optimizationSchedule;
    }

    /**
     * @return Returns the optimizationSchedule.
     */
    public String getOptimizationSchedule()
    {
        return optimizationSchedule;
    }

    /**
     * @param balkDuringOptimization The balkDuringOptimization to set.
     */
    public void setBalkDuringOptimization( boolean balkDuringOptimization )
    {
        this.balkDuringOptimization = balkDuringOptimization;
    }

    /**
     * Should we return null while optimizing the table.
     * <p>
     * @return Returns the balkDuringOptimization.
     */
    public boolean isBalkDuringOptimization()
    {
        return balkDuringOptimization;
    }
    
    /**
     * For debugging.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\nMySQLDiskCacheAttributes" );
        buf.append( "\n OptimizationSchedule [" + getOptimizationSchedule() + "]" );
        buf.append( "\n BalkDuringOptimization [" + isBalkDuringOptimization() + "]" );
        buf.append( super.toString() );
        return buf.toString();
    }    
}
