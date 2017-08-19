
package egl.http;

import egl.EagleException;
import egl.Order;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountCurrentAskOrderIds extends APIServlet.APIRequestHandler {

    static final GetAccountCurrentAskOrderIds instance = new GetAccountCurrentAskOrderIds();

    private GetAccountCurrentAskOrderIds() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.AE}, "account", "asset", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long accountId = ParameterParser.getAccountId(req, true);
        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        DbIterator<Order.Ask> askOrders;
        if (assetId == 0) {
            askOrders = Order.Ask.getAskOrdersByAccount(accountId, firstIndex, lastIndex);
        } else {
            askOrders = Order.Ask.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex);
        }
        JSONArray orderIds = new JSONArray();
        try {
            while (askOrders.hasNext()) {
                orderIds.add(Long.toUnsignedString(askOrders.next().getId()));
            }
        } finally {
            askOrders.close();
        }
        JSONObject response = new JSONObject();
        response.put("askOrderIds", orderIds);
        return response;
    }

}
