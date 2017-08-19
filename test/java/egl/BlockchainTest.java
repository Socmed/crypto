
package egl;

import egl.util.Logger;
import egl.util.Time;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.util.Properties;

public abstract class BlockchainTest extends AbstractBlockchainTest {

    protected static Tester FORGY;
    protected static Tester ALICE;
    protected static Tester BOB;
    protected static Tester CHUCK;
    protected static Tester DAVE;

    protected static int baseHeight;

    protected static String forgerSecretPhrase = "aSykrgKGZNlSVOMDxkZZgbTvQqJPGtsBggb";
    protected static final String forgerAccountId = "EGL-9KZM-KNYY-QBXZ-5TD8V";

    public static final String aliceSecretPhrase = "hope peace happen touch easy pretend worthless talk them indeed wheel state";
    private static final String bobSecretPhrase2 = "rshw9abtpsa2";
    private static final String chuckSecretPhrase = "eOdBVLMgySFvyiTy8xMuRXDTr45oTzB7L5J";
    private static final String daveSecretPhrase = "t9G2ymCmDsQij7VtYinqrbGCOAtDDA3WiNr";

    protected static boolean isEagleInitialized = false;

    public static void initEagle() {
        if (!isEagleInitialized) {
            Properties properties = ManualForgingTest.newTestProperties();
            properties.setProperty("egl.isTestnet", "true");
            properties.setProperty("egl.isOffline", "true");
            properties.setProperty("egl.enableFakeForging", "true");
            properties.setProperty("egl.fakeForgingAccount", forgerAccountId);
            properties.setProperty("egl.timeMultiplier", "1");
            properties.setProperty("egl.testnetGuaranteedBalanceConfirmations", "1");
            properties.setProperty("egl.testnetLeasingDelay", "1");
            properties.setProperty("egl.disableProcessTransactionsThread", "true");
            properties.setProperty("egl.deleteFinishedShufflings", "false");
            properties.setProperty("egl.disableSecurityPolicy", "true");
            properties.setProperty("egl.disableAdminPassword", "true");
            AbstractBlockchainTest.init(properties);
            isEagleInitialized = true;
        }
    }
    
    @BeforeClass
    public static void init() {
        initEagle();
        Eagle.setTime(new Time.CounterTime(Eagle.getEpochTime()));
        baseHeight = blockchain.getHeight();
        Logger.logMessage("baseHeight: " + baseHeight);
        FORGY = new Tester(forgerSecretPhrase);
        ALICE = new Tester(aliceSecretPhrase);
        BOB = new Tester(bobSecretPhrase2);
        CHUCK = new Tester(chuckSecretPhrase);
        DAVE = new Tester(daveSecretPhrase);
    }

    @After
    public void destroy() {
        TransactionProcessorImpl.getInstance().clearUnconfirmedTransactions();
        blockchainProcessor.popOffTo(baseHeight);
    }

    public static void generateBlock() {
        try {
            blockchainProcessor.generateBlock(forgerSecretPhrase, Eagle.getEpochTime());
        } catch (BlockchainProcessor.BlockNotAcceptedException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    public static void generateBlocks(int howMany) {
        for (int i = 0; i < howMany; i++) {
            generateBlock();
        }
    }
}
