
package egl.http;


import egl.BlockchainProcessor;
import egl.BlockchainTest;
import egl.Helper;
import egl.Eagle;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

public abstract class AbstractHttpApiSuite {
    @BeforeClass
    public static void init() {
        BlockchainTest.initEagle();
        Eagle.getTransactionProcessor().clearUnconfirmedTransactions();
        Eagle.getBlockchainProcessor().addListener(new Helper.BlockListener(), BlockchainProcessor.Event.BLOCK_GENERATED);
        Assert.assertEquals(0, Helper.getCount("unconfirmed_transaction"));
    }

    @AfterClass
    public static void shutdown() {
        Assert.assertEquals(0, Helper.getCount("unconfirmed_transaction"));
        Eagle.shutdown();
    }
}
