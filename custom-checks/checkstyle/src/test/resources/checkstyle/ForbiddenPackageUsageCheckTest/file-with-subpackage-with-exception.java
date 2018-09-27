package org.openhab.tools.analysis.checkstyle.test;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ForbiddenPackageUsageCheck;
import org.openhab.tools.analysis.checkstyle.InheritDocCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import com.google.common.utils.something;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

public class ForbiddenPackageUsageCheckTest extends AbstractStaticCheckTest {
    Configuration config = createModuleConfig(ForbiddenPackageUsageCheck.class);
    
    @Override
    protected String getPackageLocation() {
        return "checkstyle/ForbiddenPackageUsageCheckTest";
    }
    
    @Test
    public void shouldNotLogWhenThereIsNoForbiddenPackageUsage() throws Exception {
        verifyClass("validFile.java", CommonUtil.EMPTY_STRING_ARRAY);
    }
    
    private void verifyClass(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(testFileName);
        verify(config, absolutePathToTestFile, expectedMessages);
    }

}
