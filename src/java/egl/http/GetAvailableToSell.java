
package egl.http;

import egl.CurrencyExchangeOffer;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAvailableToSell extends APIServlet.APIRequestHandler {

    static final GetAvailableToSell instance = new GetAvailableToSell();

    private GetAvailableToSell() {
        super(new APITag[] {APITag.MS}, "currency", "units");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        long currencyId = ParameterParser.getUnsignedLong(req, "currency", true);
        long units = ParameterParser.getLong(req, "units", 1L, Long.MAX_VALUE, true);
        CurrencyExchangeOffer.AvailableOffers availableOffers = CurrencyExchangeOffer.getAvailableToSell(currencyId, units);
        return JSONData.availableOffers(availableOffers);
    }

}
