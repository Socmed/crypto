
package egl.http;

import egl.DigitalGoodsStore;
import egl.EagleException;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSPurchaseCount extends APIServlet.APIRequestHandler {

    static final GetDGSPurchaseCount instance = new GetDGSPurchaseCount();

    private GetDGSPurchaseCount() {
        super(new APITag[] {APITag.DGS}, "seller", "buyer", "withPublicFeedbacksOnly", "completed");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long sellerId = ParameterParser.getAccountId(req, "seller", false);
        long buyerId = ParameterParser.getAccountId(req, "buyer", false);
        final boolean completed = "true".equalsIgnoreCase(req.getParameter("completed"));
        final boolean withPublicFeedbacksOnly = "true".equalsIgnoreCase(req.getParameter("withPublicFeedbacksOnly"));

        JSONObject response = new JSONObject();
        int count;
        if (sellerId != 0 && buyerId == 0) {
            count = DigitalGoodsStore.Purchase.getSellerPurchaseCount(sellerId, withPublicFeedbacksOnly, completed);
        } else if (sellerId == 0 && buyerId != 0) {
            count = DigitalGoodsStore.Purchase.getBuyerPurchaseCount(buyerId, withPublicFeedbacksOnly, completed);
        } else if (sellerId == 0 && buyerId == 0) {
            count = DigitalGoodsStore.Purchase.getCount(withPublicFeedbacksOnly, completed);
        } else {
            count = DigitalGoodsStore.Purchase.getSellerBuyerPurchaseCount(sellerId, buyerId, withPublicFeedbacksOnly, completed);
        }
        response.put("numberOfPurchases", count);
        return response;
    }

}
