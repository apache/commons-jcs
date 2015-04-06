package org.apache.commons.jcs.auxiliary.disk.block;

import org.apache.commons.jcs.auxiliary.disk.behavior.IDiskCacheAttributes.DiskLimitType;

public class BlockDiskCacheUnitTestSize extends BlockDiskCacheUnitTest {

	@Override
	public BlockDiskCacheAttributes getCacheAttributes() {
		BlockDiskCacheAttributes ret = new BlockDiskCacheAttributes();
		ret.setDiskLimitType(DiskLimitType.SIZE);
		return ret;
	}

}
