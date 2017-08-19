
package egl.http.monetarysystem;

import egl.AccountCurrencyBalance;
import egl.BlockchainTest;
import egl.Constants;
import egl.CurrencyType;
import egl.http.APICall;
import egl.util.Convert;
import egl.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCurrencyExchange extends BlockchainTest {

    @Test
    public void buyCurrency() {
        APICall apiCall1 = new TestCurrencyIssuance.Builder().type(CurrencyType.EXCHANGEABLE.getCode()).build();
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall1);
        AccountCurrencyBalance initialSellerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId);
        AccountCurrencyBalance initialBuyerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId);

        Assert.assertEquals(100000, initialSellerBalance.getCurrencyUnits());
        Assert.assertEquals(100000, initialSellerBalance.getUnconfirmedCurrencyUnits());

        JSONObject publishExchangeOfferResponse = publishExchangeOffer(currencyId);

        generateBlock();

        APICall apiCall = new APICall.Builder("getBuyOffers").param("currency", currencyId).build();
        JSONObject getAllOffersResponse = apiCall.invoke();
        Logger.logDebugMessage("getAllOffersResponse:" + getAllOffersResponse.toJSONString());
        JSONArray offer = (JSONArray)getAllOffersResponse.get("offers");
        Assert.assertEquals(publishExchangeOfferResponse.get("transaction"), ((JSONObject)offer.get(0)).get("offer"));

        // The buy offer reduces the unconfirmed balance but does not change the confirmed balance
        // The sell offer reduces the unconfirmed currency units and confirmed units
        AccountCurrencyBalance afterOfferSellerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId);
        Assert.assertEquals(new AccountCurrencyBalance(-1000*95 - Constants.ONE_EGL, -Constants.ONE_EGL, -500, 0),
                afterOfferSellerBalance.diff(initialSellerBalance));

        // buy at rate higher than sell offer results in selling at sell offer
        apiCall = new APICall.Builder("currencyBuy").
                secretPhrase(BOB.getSecretPhrase()).feeNQT(Constants.ONE_EGL).
                param("currency", currencyId).
                param("rateNQT", "" + 106).
                param("units", "200").
                build();
        JSONObject currencyExchangeResponse = apiCall.invoke();
        Logger.logDebugMessage("currencyExchangeResponse:" + currencyExchangeResponse);
        generateBlock();

        AccountCurrencyBalance afterBuySellerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId);
        Assert.assertEquals(new AccountCurrencyBalance(2000, 200 * 105, 0, -200),
                afterBuySellerBalance.diff(afterOfferSellerBalance));

        AccountCurrencyBalance afterBuyBuyerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId);
        Assert.assertEquals(new AccountCurrencyBalance(-200*105 - Constants.ONE_EGL, -200*105 - Constants.ONE_EGL, 200, 200),
                afterBuyBuyerBalance.diff(initialBuyerBalance));

        apiCall = new APICall.Builder("getAllExchanges").build();
        JSONObject getAllExchangesResponse = apiCall.invoke();
        Logger.logDebugMessage("getAllExchangesResponse: " + getAllExchangesResponse);
        JSONArray exchanges = (JSONArray)getAllExchangesResponse.get("exchanges");
        JSONObject exchange = (JSONObject) exchanges.get(0);
        Assert.assertEquals("105", exchange.get("rateNQT"));
        Assert.assertEquals("200", exchange.get("units"));
        Assert.assertEquals(currencyId, exchange.get("currency"));
        Assert.assertEquals(initialSellerBalance.getAccountId(), Convert.parseUnsignedLong((String)exchange.get("seller")));
        Assert.assertEquals(initialBuyerBalance.getAccountId(), Convert.parseUnsignedLong((String)exchange.get("buyer")));
    }

    @Test
    public void sellCurrency() {
        APICall apiCall1 = new TestCurrencyIssuance.Builder().type(CurrencyType.EXCHANGEABLE.getCode()).build();
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall1);
        AccountCurrencyBalance initialBuyerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId);
        AccountCurrencyBalance initialSellerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId);

        Assert.assertEquals(100000, initialBuyerBalance.getCurrencyUnits());
        Assert.assertEquals(100000, initialBuyerBalance.getUnconfirmedCurrencyUnits());

        JSONObject publishExchangeOfferResponse = publishExchangeOffer(currencyId);

        generateBlock();

        APICall apiCall = new APICall.Builder("getSellOffers").param("currency", currencyId).build();
        JSONObject getAllOffersResponse = apiCall.invoke();
        Logger.logDebugMessage("getAllOffersResponse:" + getAllOffersResponse.toJSONString());
        JSONArray offer = (JSONArray)getAllOffersResponse.get("offers");
        Assert.assertEquals(publishExchangeOfferResponse.get("transaction"), ((JSONObject)offer.get(0)).get("offer"));

        // The buy offer reduces the unconfirmed balance but does not change the confirmed balance
        // The sell offer reduces the unconfirmed currency units and confirmed units
        AccountCurrencyBalance afterOfferBuyerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId);
        Assert.assertEquals(new AccountCurrencyBalance(-1000 * 95 - Constants.ONE_EGL, -Constants.ONE_EGL, -500, 0),
                afterOfferBuyerBalance.diff(initialBuyerBalance));

        // We now transfer 2000 units to the 2nd account so that this account can sell them for EGL
        apiCall = new APICall.Builder("transferCurrency").
                secretPhrase(ALICE.getSecretPhrase()).feeNQT(Constants.ONE_EGL).
                param("currency", currencyId).
                param("recipient", Long.toUnsignedString(initialSellerBalance.getAccountId())).
                param("units", "2000").
                build();
        apiCall.invoke();
        generateBlock();

        AccountCurrencyBalance afterTransferBuyerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId);
        Assert.assertEquals(new AccountCurrencyBalance(-Constants.ONE_EGL, -Constants.ONE_EGL, -2000, -2000),
                afterTransferBuyerBalance.diff(afterOfferBuyerBalance));

        AccountCurrencyBalance afterTransferSellerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId);
        Assert.assertEquals(new AccountCurrencyBalance(0, 0, 2000, 2000),
                afterTransferSellerBalance.diff(initialSellerBalance));

        // sell at rate lower than buy offer results in selling at buy offer rate (95)
        apiCall = new APICall.Builder("currencySell").
                secretPhrase(BOB.getSecretPhrase()).feeNQT(Constants.ONE_EGL).
                param("currency", currencyId).
                param("rateNQT", "" + 90).
                param("units", "200").
                build();
        JSONObject currencyExchangeResponse = apiCall.invoke();
        Logger.logDebugMessage("currencyExchangeResponse:" + currencyExchangeResponse);
        generateBlock();

        // the seller receives 200*95=19000 for 200 units
        AccountCurrencyBalance afterBuyBuyerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId);
        Assert.assertEquals(new AccountCurrencyBalance(0, -19000, 0, 200),
                afterBuyBuyerBalance.diff(afterTransferBuyerBalance));

        AccountCurrencyBalance afterBuySellerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId);
        Assert.assertEquals(new AccountCurrencyBalance(19000-Constants.ONE_EGL, 19000-Constants.ONE_EGL, -200, -200),
                afterBuySellerBalance.diff(afterTransferSellerBalance));

        apiCall = new APICall.Builder("getAllExchanges").build();
        JSONObject getAllExchangesResponse = apiCall.invoke();
        Logger.logDebugMessage("getAllExchangesResponse: " + getAllExchangesResponse);
        JSONArray exchanges = (JSONArray)getAllExchangesResponse.get("exchanges");
        JSONObject exchange = (JSONObject) exchanges.get(0);
        Assert.assertEquals("95", exchange.get("rateNQT"));
        Assert.assertEquals("200", exchange.get("units"));
        Assert.assertEquals(currencyId, exchange.get("currency"));
        Assert.assertEquals(initialSellerBalance.getAccountId(), Convert.parseUnsignedLong((String) exchange.get("seller")));
        Assert.assertEquals(initialBuyerBalance.getAccountId(), Convert.parseUnsignedLong((String)exchange.get("buyer")));
    }

    private JSONObject publishExchangeOffer(String currencyId) {
        APICall apiCall = new APICall.Builder("publishExchangeOffer").
                secretPhrase(ALICE.getSecretPhrase()).feeNQT(Constants.ONE_EGL).
                param("deadline", "1440").
                param("currency", currencyId).
                param("buyRateNQT", "" + 95). // buy currency for EGL
                param("sellRateNQT", "" + 105). // sell currency for EGL
                param("totalBuyLimit", "10000").
                param("totalSellLimit", "5000").
                param("initialBuySupply", "1000").
                param("initialSellSupply", "500").
                param("expirationHeight", "" + Integer.MAX_VALUE).
                build();

        JSONObject publishExchangeOfferResponse = apiCall.invoke();
        Logger.logDebugMessage("publishExchangeOfferResponse: " + publishExchangeOfferResponse.toJSONString());
        return publishExchangeOfferResponse;
    }


}
