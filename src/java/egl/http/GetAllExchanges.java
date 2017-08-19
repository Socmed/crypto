
package egl.http;

import egl.Exchange;
import egl.EagleException;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAllExchanges extends APIServlet.APIRequestHandler {

    static final GetAllExchanges instance = new GetAllExchanges();

    private GetAllExchanges() {
        super(new APITag[] {APITag.MS}, "timestamp", "firstIndex", "lastIndex", "includeCurrencyInfo");
    }
    
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        final int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeCurrencyInfo = "true".equalsIgnoreCase(req.getParameter("includeCurrencyInfo"));

        JSONObject response = new JSONObject();
        JSONArray exchanges = new JSONArray();
        try (DbIterator<Exchange> exchangeIterator = Exchange.getAllExchanges(firstIndex, lastIndex)) {
            while (exchangeIterator.hasNext()) {
                Exchange exchange = exchangeIterator.next();
                if (exchange.getTimestamp() < timestamp) {
                    break;
                }
                exchanges.add(JSONData.exchange(exchange, includeCurrencyInfo));
            }
        }
        response.put("exchanges", exchanges);
        return response;
    }

}
