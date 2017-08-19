
package egl.http;

import egl.Exchange;
import egl.db.DbIterator;
import egl.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.MISSING_TRANSACTION;

public final class GetExchangesByExchangeRequest extends APIServlet.APIRequestHandler {

    static final GetExchangesByExchangeRequest instance = new GetExchangesByExchangeRequest();

    private GetExchangesByExchangeRequest() {
        super(new APITag[] {APITag.MS}, "transaction", "includeCurrencyInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        String transactionIdString = Convert.emptyToNull(req.getParameter("transaction"));
        if (transactionIdString == null) {
            return MISSING_TRANSACTION;
        }
        long transactionId = Convert.parseUnsignedLong(transactionIdString);
        boolean includeCurrencyInfo = "true".equalsIgnoreCase(req.getParameter("includeCurrencyInfo"));
        JSONObject response = new JSONObject();
        JSONArray exchangesData = new JSONArray();
        try (DbIterator<Exchange> exchanges = Exchange.getExchanges(transactionId)) {
            while (exchanges.hasNext()) {
                exchangesData.add(JSONData.exchange(exchanges.next(), includeCurrencyInfo));
            }
        }
        response.put("exchanges", exchangesData);
        return response;
    }

}
