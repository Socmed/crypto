
package egl.http;

import egl.DigitalGoodsStore;
import egl.EagleException;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSGoodsPurchaseCount extends APIServlet.APIRequestHandler {

    static final GetDGSGoodsPurchaseCount instance = new GetDGSGoodsPurchaseCount();

    private GetDGSGoodsPurchaseCount() {
        super(new APITag[] {APITag.DGS}, "goods", "withPublicFeedbacksOnly", "completed");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long goodsId = ParameterParser.getUnsignedLong(req, "goods", true);
        final boolean withPublicFeedbacksOnly = "true".equalsIgnoreCase(req.getParameter("withPublicFeedbacksOnly"));
        final boolean completed = "true".equalsIgnoreCase(req.getParameter("completed"));

        JSONObject response = new JSONObject();
        response.put("numberOfPurchases", DigitalGoodsStore.Purchase.getGoodsPurchaseCount(goodsId, withPublicFeedbacksOnly, completed));
        return response;

    }

}
