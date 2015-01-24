package org.bitcoin.authenticator.core.GcmUtil;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by alonmuroch on 1/24/15.
 */
public class GcmUtilGlobalTest extends TestCase {
    @Test
    public void testApiConsoleProjectNumber() {
        assertNull(GcmUtilGlobal.API_CONSOLE_PROJECT_NUMBER); // should be null, is loaded from a attrs.xml
    }
}
