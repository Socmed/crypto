
package egl.http;

import egl.Attachment;
import egl.Eagle;
import egl.EagleException;
import egl.Order;
import egl.Transaction;
import egl.TransactionType;
import egl.db.DbIterator;
import egl.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

public final class GetAskOrders extends APIServlet.APIRequestHandler {

    static final GetAskOrders instance = new GetAskOrders();

    private GetAskOrders() {
        super(new APITag[] {APITag.AE}, "asset", "firstIndex", "lastIndex", "showExpectedCancellations");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", true);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean showExpectedCancellations = "true".equalsIgnoreCase(req.getParameter("showExpectedCancellations"));

        long[] cancellations = null;
        if (showExpectedCancellations) {
            Filter<Transaction> filter = transaction -> transaction.getType() == TransactionType.ColoredCoins.ASK_ORDER_CANCELLATION;
            List<? extends Transaction> transactions = Eagle.getBlockchain().getExpectedTransactions(filter);
            cancellations = new long[transactions.size()];
            for (int i = 0; i < transactions.size(); i++) {
                Attachment.ColoredCoinsOrderCancellation attachment = (Attachment.ColoredCoinsOrderCancellation) transactions.get(i).getAttachment();
                cancellations[i] = attachment.getOrderId();
            }
            Arrays.sort(cancellations);
        }

        JSONArray orders = new JSONArray();
        try (DbIterator<Order.Ask> askOrders = Order.Ask.getSortedOrders(assetId, firstIndex, lastIndex)) {
            while (askOrders.hasNext()) {
                Order.Ask order = askOrders.next();
                JSONObject orderJSON = JSONData.askOrder(order);
                if (showExpectedCancellations && Arrays.binarySearch(cancellations, order.getId()) >= 0) {
                    orderJSON.put("expectedCancellation", Boolean.TRUE);
                }
                orders.add(orderJSON);
            }
        }

        JSONObject response = new JSONObject();
        response.put("askOrders", orders);
        return response;

    }

}
