
package egl.http;

import egl.BlockchainTest;
import egl.Constants;
import egl.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class LeaseTest extends BlockchainTest {

    @Test
    public void lease() {
        // #2 & #3 lease their balance to %1
        JSONObject response = new APICall.Builder("leaseBalance").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("period", "2").
                param("feeNQT", Constants.ONE_EGL).
                build().invoke();
        Logger.logDebugMessage("leaseBalance: " + response);
        response = new APICall.Builder("leaseBalance").
                param("secretPhrase", CHUCK.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("period", "3").
                param("feeNQT", Constants.ONE_EGL).
                build().invoke();
        Logger.logDebugMessage("leaseBalance: " + response);
        generateBlock();

        // effective balance hasn't changed since lease is not in effect yet
        JSONObject lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals(ALICE.getInitialEffectiveBalance(), lesseeResponse.get("effectiveBalanceEGL"));

        // lease is registered
        JSONObject leasedResponse1 = new APICall.Builder("getAccount").
                param("account", BOB.getRsAccount()).
                build().invoke();
        Logger.logDebugMessage("getLeasedAccount: " + leasedResponse1);
        Assert.assertEquals(ALICE.getRsAccount(), leasedResponse1.get("currentLesseeRS"));
        Assert.assertEquals((long) (baseHeight + 1 + 1), leasedResponse1.get("currentLeasingHeightFrom"));
        Assert.assertEquals((long) (baseHeight + 1 + 1 + 2), leasedResponse1.get("currentLeasingHeightTo"));
        JSONObject leasedResponse2 = new APICall.Builder("getAccount").
                param("account", CHUCK.getRsAccount()).
                build().invoke();
        Logger.logDebugMessage("getLeasedAccount: " + leasedResponse1);
        Assert.assertEquals(ALICE.getRsAccount(), leasedResponse2.get("currentLesseeRS"));
        Assert.assertEquals((long) (baseHeight + 1 + 1), leasedResponse2.get("currentLeasingHeightFrom"));
        Assert.assertEquals((long) (baseHeight + 1 + 1 + 3), leasedResponse2.get("currentLeasingHeightTo"));
        generateBlock();


        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance() + BOB.getInitialBalance() + CHUCK.getInitialBalance()) / Constants.ONE_EGL - 2,
                lesseeResponse.get("effectiveBalanceEGL"));
        generateBlock();
        generateBlock();
        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance() + CHUCK.getInitialBalance()) / Constants.ONE_EGL - 1 /* fees */,
                lesseeResponse.get("effectiveBalanceEGL"));
        generateBlock();
        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance()) / Constants.ONE_EGL,
                lesseeResponse.get("effectiveBalanceEGL"));
    }
}
