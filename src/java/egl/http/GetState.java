
package egl.http;

import egl.Account;
import egl.AccountRestrictions;
import egl.Alias;
import egl.Asset;
import egl.AssetTransfer;
import egl.Constants;
import egl.Currency;
import egl.CurrencyBuyOffer;
import egl.CurrencyTransfer;
import egl.DigitalGoodsStore;
import egl.Exchange;
import egl.ExchangeRequest;
import egl.Generator;
import egl.Eagle;
import egl.Order;
import egl.Poll;
import egl.PrunableMessage;
import egl.Shuffling;
import egl.TaggedData;
import egl.Trade;
import egl.Vote;
import egl.peer.Peers;
import egl.util.UPnP;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

public final class GetState extends APIServlet.APIRequestHandler {

    static final GetState instance = new GetState();

    private GetState() {
        super(new APITag[] {APITag.INFO}, "includeCounts", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        JSONObject response = GetBlockchainStatus.instance.processRequest(req);

        if ("true".equalsIgnoreCase(req.getParameter("includeCounts")) && API.checkPassword(req)) {
            response.put("numberOfTransactions", Eagle.getBlockchain().getTransactionCount());
            response.put("numberOfAccounts", Account.getCount());
            response.put("numberOfAssets", Asset.getCount());
            int askCount = Order.Ask.getCount();
            int bidCount = Order.Bid.getCount();
            response.put("numberOfOrders", askCount + bidCount);
            response.put("numberOfAskOrders", askCount);
            response.put("numberOfBidOrders", bidCount);
            response.put("numberOfTrades", Trade.getCount());
            response.put("numberOfTransfers", AssetTransfer.getCount());
	        response.put("numberOfCurrencies", Currency.getCount());
    	    response.put("numberOfOffers", CurrencyBuyOffer.getCount());
            response.put("numberOfExchangeRequests", ExchangeRequest.getCount());
        	response.put("numberOfExchanges", Exchange.getCount());
        	response.put("numberOfCurrencyTransfers", CurrencyTransfer.getCount());
            response.put("numberOfAliases", Alias.getCount());
            response.put("numberOfGoods", DigitalGoodsStore.Goods.getCount());
            response.put("numberOfPurchases", DigitalGoodsStore.Purchase.getCount());
            response.put("numberOfTags", DigitalGoodsStore.Tag.getCount());
            response.put("numberOfPolls", Poll.getCount());
            response.put("numberOfVotes", Vote.getCount());
            response.put("numberOfPrunableMessages", PrunableMessage.getCount());
            response.put("numberOfTaggedData", TaggedData.getCount());
            response.put("numberOfDataTags", TaggedData.Tag.getTagCount());
            response.put("numberOfAccountLeases", Account.getAccountLeaseCount());
            response.put("numberOfActiveAccountLeases", Account.getActiveLeaseCount());
            response.put("numberOfShufflings", Shuffling.getCount());
            response.put("numberOfActiveShufflings", Shuffling.getActiveCount());
            response.put("numberOfPhasingOnlyAccounts", AccountRestrictions.PhasingOnly.getCount());
        }
        response.put("numberOfPeers", Peers.getAllPeers().size());
        response.put("numberOfActivePeers", Peers.getActivePeers().size());
        response.put("numberOfUnlockedAccounts", Generator.getAllGenerators().size());
        response.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        response.put("maxMemory", Runtime.getRuntime().maxMemory());
        response.put("totalMemory", Runtime.getRuntime().totalMemory());
        response.put("freeMemory", Runtime.getRuntime().freeMemory());
        response.put("peerPort", Peers.getDefaultPeerPort());
        response.put("isOffline", Constants.isOffline);
        response.put("needsAdminPassword", !API.disableAdminPassword);
        response.put("customLoginWarning", Constants.customLoginWarning);
        InetAddress externalAddress = UPnP.getExternalAddress();
        if (externalAddress != null) {
            response.put("upnpExternalAddress", externalAddress.getHostAddress());
        }
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}
