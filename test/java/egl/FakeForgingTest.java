
package egl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class FakeForgingTest extends AbstractForgingTest {

    @Before
    public void init() {
        Properties properties = AbstractForgingTest.newTestProperties();
        properties.setProperty("egl.disableGenerateBlocksThread", "false");
        properties.setProperty("egl.enableFakeForging", "true");
        properties.setProperty("egl.timeMultiplier", "1");
        AbstractForgingTest.init(properties);
        Assert.assertTrue("egl.fakeForgingAccount must be defined in egl.properties", Eagle.getStringProperty("egl.fakeForgingAccount") != null);
    }

    @Test
    public void fakeForgingTest() {
        forgeTo(startHeight + 10, testForgingSecretPhrase);
    }

    @After
    public void destroy() {
        AbstractForgingTest.shutdown();
    }

}
