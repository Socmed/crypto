
package egl.http;

import egl.Currency;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAllCurrencies extends APIServlet.APIRequestHandler {

    static final GetAllCurrencies instance = new GetAllCurrencies();

    private GetAllCurrencies() {
        super(new APITag[] {APITag.MS}, "firstIndex", "lastIndex", "includeCounts");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));

        JSONObject response = new JSONObject();
        JSONArray currenciesJSONArray = new JSONArray();
        response.put("currencies", currenciesJSONArray);
        try (DbIterator<Currency> currencies = Currency.getAllCurrencies(firstIndex, lastIndex)) {
            for (Currency currency : currencies) {
                currenciesJSONArray.add(JSONData.currency(currency, includeCounts));
            }
        }
        return response;
    }

}
