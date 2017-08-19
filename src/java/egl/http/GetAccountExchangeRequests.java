
package egl.http;

import egl.ExchangeRequest;
import egl.EagleException;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountExchangeRequests extends APIServlet.APIRequestHandler {

    static final GetAccountExchangeRequests instance = new GetAccountExchangeRequests();

    private GetAccountExchangeRequests() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.MS}, "account", "currency", "includeCurrencyInfo", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long accountId = ParameterParser.getAccountId(req, true);
        long currencyId = ParameterParser.getUnsignedLong(req, "currency", true);
        boolean includeCurrencyInfo = "true".equalsIgnoreCase(req.getParameter("includeCurrencyInfo"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray jsonArray = new JSONArray();
        try (DbIterator<ExchangeRequest> exchangeRequests = ExchangeRequest.getAccountCurrencyExchangeRequests(accountId, currencyId,
                firstIndex, lastIndex)) {
            while (exchangeRequests.hasNext()) {
                jsonArray.add(JSONData.exchangeRequest(exchangeRequests.next(), includeCurrencyInfo));
            }
        }
        JSONObject response = new JSONObject();
        response.put("exchangeRequests", jsonArray);
        return response;
    }

}
