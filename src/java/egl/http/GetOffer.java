
package egl.http;

import egl.CurrencyBuyOffer;
import egl.CurrencySellOffer;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetOffer extends APIServlet.APIRequestHandler {

    static final GetOffer instance = new GetOffer();

    private GetOffer() {
        super(new APITag[] {APITag.MS}, "offer");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        JSONObject response = new JSONObject();
        CurrencyBuyOffer buyOffer = ParameterParser.getBuyOffer(req);
        CurrencySellOffer sellOffer = ParameterParser.getSellOffer(req);
        response.put("buyOffer", JSONData.offer(buyOffer));
        response.put("sellOffer", JSONData.offer(sellOffer));
        return response;
    }

}
