package org.apache.jcs.engine.stats;

import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author aaronsm
 *
 */
public class Stats implements IStats {

	private IStatElement[] stats = null;	
	
	private String typeName = null;
	
	/* (non-Javadoc)
	 * @see org.apache.jcs.engine.stats.behavior.IStats#getStatElements()
	 */
	public IStatElement[] getStatElements() {
		return stats;
	}

	/* (non-Javadoc)
	 * @see org.apache.jcs.engine.stats.behavior.IStats#setStatElements(org.apache.jcs.engine.stats.behavior.IStatElement[])
	 */
	public void setStatElements(IStatElement[] stats) {
		this.stats = stats;
	}

	/* (non-Javadoc)
	 * @see org.apache.jcs.engine.stats.behavior.IStats#getTypeName()
	 */
	public String getTypeName() {
		return typeName;
	}

	/* (non-Javadoc)
	 * @see org.apache.jcs.engine.stats.behavior.IStats#setTypeName(java.lang.String)
	 */
	public void setTypeName(String name) {
		typeName = name;
	}


	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		buf.append( typeName );
		
		if ( stats != null )
		{
			for( int i = 0; i < stats.length; i++ )
			{
				buf.append( "\n" );
				buf.append( stats[i] );
			}
		}
		
		return buf.toString();
	}
	
}
