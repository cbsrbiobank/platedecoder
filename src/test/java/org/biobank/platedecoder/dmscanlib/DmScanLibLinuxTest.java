package org.biobank.platedecoder.dmscanlib;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.biobank.platedecoder.dmscanlib.ScanLib.ResultCode.*;

public class DmScanLibLinuxTest extends RequiresJniLibraryTest {

    // private static Logger log = LoggerFactory.getLogger(TestDmScanLibLinux.class);

    @Before
    public void beforeMethod() {
        // this test is valid only when not running on windows
        Assume.assumeTrue(!LibraryLoader.runningMsWindows());
    }

    @Test
    public void linuxEmptyImplementationJNI() throws Exception {
        ScanLib scanLib = ScanLib.getInstance();
        ScanLibResult r = scanLib.getScannerCapability();
        Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());
        Assert.assertEquals(SC_FAIL, r.getValue());

        r = scanLib.scanImage(0, 0, 0, 0, 0, 0, 0, 0, "tmp.txt");
        Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());
        Assert.assertEquals(SC_FAIL, r.getValue());

        r = scanLib.scanFlatbed(0, 0, 0, 0, "tmp.txt");
        Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());
        Assert.assertEquals(SC_FAIL, r.getValue());

        r = scanLib.scanAndDecode(0, 0, 0, 0, 0, 0, 0, 0, null, new CellRectangle[] {});
        Assert.assertEquals(ScanLibResult.Result.FAIL, r.getResultCode());
        Assert.assertEquals(SC_FAIL, r.getValue());
    }
}
