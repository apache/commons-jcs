package org.apache.jcs.auxiliary;

/** For testing. */
public class MockAuxiliaryCacheAttributes
    extends AbstractAuxiliaryCacheAttributes
{
    /** Don't change. */
    private static final long serialVersionUID = 1091238902450504108L;

    /**
     * Doesn't really copy
     * <p>
     * @return this
     */
    public AuxiliaryCacheAttributes copy()
    {
        return this;
    }

}
