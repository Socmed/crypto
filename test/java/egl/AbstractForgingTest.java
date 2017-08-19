
package egl;

import org.junit.Assert;

import java.util.Properties;

public abstract class AbstractForgingTest extends AbstractBlockchainTest {

    protected static final int minStartHeight = 150000;
    protected static int startHeight;
    protected final static String testForgingSecretPhrase = "aSykrgKGZNlSVOMDxkZZgbTvQqJPGtsBggb";

    protected static Properties newTestProperties() {
        Properties properties = AbstractBlockchainTest.newTestProperties();
        properties.setProperty("egl.isTestnet", "true");
        properties.setProperty("egl.isOffline", "true");
        return properties;
    }

    protected static void init(Properties properties) {
        AbstractBlockchainTest.init(properties);
        startHeight = blockchain.getHeight();
        Assert.assertTrue(startHeight >= minStartHeight);
    }

    protected static void shutdown() {
        blockchainProcessor.popOffTo(startHeight);
        AbstractBlockchainTest.shutdown();
    }

}
