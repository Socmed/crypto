
package egl.http.monetarysystem;

import egl.Account;
import egl.BlockchainTest;
import egl.Constants;
import egl.CurrencyType;
import egl.crypto.Crypto;
import egl.http.APICall;
import egl.util.Convert;
import egl.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCurrencyReserveAndClaim extends BlockchainTest {

    @Test
    public void reserveIncrease() {
        APICall apiCall = new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                issuanceHeight(baseHeight + 5).
                minReservePerUnitNQT((long) 1).
                initialSupply((long)0).
                reserveSupply((long)100000).
                build();
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall);
        reserveIncreaseImpl(currencyId, ALICE.getSecretPhrase(), BOB.getSecretPhrase());
    }

    @Test
    public void cancelCrowdFunding() {
        APICall apiCall1 = new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                issuanceHeight(baseHeight + 4).
                minReservePerUnitNQT((long) 11).
                initialSupply((long)0).
                reserveSupply((long)100000).
                build();
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall1);
        long balanceNQT1 = ALICE.getBalance();
        long balanceNQT2 = BOB.getBalance();
        reserveIncreaseImpl(currencyId, ALICE.getSecretPhrase(), BOB.getSecretPhrase());
        generateBlock(); // cancellation of crowd funding because of insufficient funds
        APICall apiCall = new APICall.Builder("getCurrencyFounders").
                feeNQT(Constants.ONE_EGL).
                param("currency", currencyId).
                build();
        JSONObject getFoundersResponse = apiCall.invoke();
        Logger.logMessage("getFoundersResponse: " + getFoundersResponse);
        Assert.assertTrue(((JSONArray)getFoundersResponse.get("founders")).size() == 0);
        Assert.assertEquals(balanceNQT1 - Constants.ONE_EGL, ALICE.getBalance());
        Assert.assertEquals(balanceNQT2 - 2*Constants.ONE_EGL, BOB.getBalance());
    }

    @Test
    public void crowdFundingDistribution() {
        APICall apiCall = new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                initialSupply((long) 0).
                reserveSupply((long) 100000).
                issuanceHeight(baseHeight + 4).
                minReservePerUnitNQT((long) 10).
                build();

        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall);
        long balanceNQT1 = ALICE.getBalance();
        long balanceNQT2 = BOB.getBalance();
        reserveIncreaseImpl(currencyId, ALICE.getSecretPhrase(), BOB.getSecretPhrase());
        generateBlock(); // distribution of currency to founders
        Assert.assertEquals(20000, ALICE.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(80000, BOB.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(balanceNQT1 - Constants.ONE_EGL - 200000 + (100000*10), ALICE.getBalance());
        Assert.assertEquals(balanceNQT2 - 2*Constants.ONE_EGL - 800000, BOB.getBalance());
    }

    @Test
    public void crowdFundingDistributionRounding() {
        APICall apiCall = new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                initialSupply((long)0).
                reserveSupply((long)24).
                maxSupply((long) 24).
                issuanceHeight(baseHeight + 4).
                minReservePerUnitNQT((long) 10).
                build();

        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall);
        long balanceNQT1 = ALICE.getBalance();
        long balanceNQT2 = BOB.getBalance();
        long balanceNQT3 = CHUCK.getBalance();
        reserveIncreaseImpl(currencyId, BOB.getSecretPhrase(), CHUCK.getSecretPhrase());
        generateBlock(); // distribution of currency to founders

        // account 2 balance round(24 * 0.2) = round(4.8) = 4
        // account 3 balance round(24 * 0.8) = round(19.2) = 19
        // issuer receives the leftover of 1
        Assert.assertEquals(4, BOB.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(19, CHUCK.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(1, ALICE.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(balanceNQT1 + 24 * 10, ALICE.getBalance());
        Assert.assertEquals(balanceNQT2 - Constants.ONE_EGL - 24 * 2, BOB.getBalance());
        Assert.assertEquals(balanceNQT3 - 2 * Constants.ONE_EGL - 24 * 8, CHUCK.getBalance());

        apiCall = new APICall.Builder("getCurrency").
                param("currency", currencyId).
                build();
        JSONObject response = apiCall.invoke();
        Assert.assertEquals("24", response.get("currentSupply"));
    }

    public void reserveIncreaseImpl(String currencyId, String secret1, String secret2) {
        APICall apiCall = new APICall.Builder("currencyReserveIncrease").
                secretPhrase(secret1).
                feeNQT(Constants.ONE_EGL).
                param("currency", currencyId).
                param("amountPerUnitNQT", "" + 2).
                build();
        JSONObject reserveIncreaseResponse = apiCall.invoke();
        Logger.logMessage("reserveIncreaseResponse: " + reserveIncreaseResponse);
        generateBlock();

        // Two increase reserve transactions in the same block
        apiCall = new APICall.Builder("currencyReserveIncrease").
                secretPhrase(secret2).
                feeNQT(Constants.ONE_EGL).
                param("currency", currencyId).
                param("amountPerUnitNQT", "" + 3).
                build();
        reserveIncreaseResponse = apiCall.invoke();
        Logger.logMessage("reserveIncreaseResponse: " + reserveIncreaseResponse);

        apiCall = new APICall.Builder("currencyReserveIncrease").
                secretPhrase(secret2).
                feeNQT(Constants.ONE_EGL).
                param("currency", currencyId).
                param("amountPerUnitNQT", "" + 5).
                build();
        reserveIncreaseResponse = apiCall.invoke();
        Logger.logMessage("reserveIncreaseResponse: " + reserveIncreaseResponse);

        generateBlock();

        apiCall = new APICall.Builder("getCurrencyFounders").
                feeNQT(Constants.ONE_EGL).
                param("currency", currencyId).
                build();
        JSONObject getFoundersResponse = apiCall.invoke();
        Logger.logMessage("getFoundersResponse: " + getFoundersResponse);

        JSONArray founders = (JSONArray)getFoundersResponse.get("founders");
        JSONObject founder1 = (JSONObject)founders.get(0);
        Assert.assertTrue(Long.toUnsignedString(Account.getId(Crypto.getPublicKey(secret1))).equals(founder1.get("account")) ||
                Long.toUnsignedString(Account.getId(Crypto.getPublicKey(secret2))).equals(founder1.get("account")));
        Assert.assertTrue(String.valueOf(3L + 5L).equals(founder1.get("amountPerUnitNQT")) || String.valueOf(2L).equals(founder1.get("amountPerUnitNQT")));

        JSONObject founder2 = (JSONObject)founders.get(1);
        Assert.assertTrue(Long.toUnsignedString(Account.getId(Crypto.getPublicKey(secret1))).equals(founder2.get("account")) ||
                Long.toUnsignedString(Account.getId(Crypto.getPublicKey(secret2))).equals(founder2.get("account")));
        Assert.assertTrue(String.valueOf(3L + 5L).equals(founder2.get("amountPerUnitNQT")) || String.valueOf(2L).equals(founder2.get("amountPerUnitNQT")));
    }

}
