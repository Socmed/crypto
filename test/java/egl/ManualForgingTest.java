
package egl;

import egl.crypto.Crypto;
import egl.util.Time;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class ManualForgingTest extends AbstractForgingTest {

    @Test
    public void manualForgingTest() {
        Properties properties = ManualForgingTest.newTestProperties();
        properties.setProperty("egl.enableFakeForging", "true");
        properties.setProperty("egl.timeMultiplier", "1");
        AbstractForgingTest.init(properties);
        Assert.assertTrue("egl.fakeForgingAccount must be defined in egl.properties", Eagle.getStringProperty("egl.fakeForgingAccount") != null);
        final byte[] testPublicKey = Crypto.getPublicKey(testForgingSecretPhrase);
        Eagle.setTime(new Time.CounterTime(Eagle.getEpochTime()));
        try {
            for (int i = 0; i < 10; i++) {
                blockchainProcessor.generateBlock(testForgingSecretPhrase, Eagle.getEpochTime());
                Assert.assertArrayEquals(testPublicKey, blockchain.getLastBlock().getGeneratorPublicKey());
            }
        } catch (BlockchainProcessor.BlockNotAcceptedException e) {
            throw new RuntimeException(e.toString(), e);
        }
        Assert.assertEquals(startHeight + 10, blockchain.getHeight());
        AbstractForgingTest.shutdown();
    }

}
