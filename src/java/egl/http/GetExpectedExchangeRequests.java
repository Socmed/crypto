
package egl.http;

import egl.Attachment;
import egl.MonetarySystem;
import egl.Eagle;
import egl.EagleException;
import egl.Transaction;
import egl.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public final class GetExpectedExchangeRequests extends APIServlet.APIRequestHandler {

    static final GetExpectedExchangeRequests instance = new GetExpectedExchangeRequests();

    private GetExpectedExchangeRequests() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.MS}, "account", "currency", "includeCurrencyInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long accountId = ParameterParser.getAccountId(req, "account", false);
        long currencyId = ParameterParser.getUnsignedLong(req, "currency", false);
        boolean includeCurrencyInfo = "true".equalsIgnoreCase(req.getParameter("includeCurrencyInfo"));

        Filter<Transaction> filter = transaction -> {
            if (transaction.getType() != MonetarySystem.EXCHANGE_BUY && transaction.getType() != MonetarySystem.EXCHANGE_SELL) {
                return false;
            }
            if (accountId != 0 && transaction.getSenderId() != accountId) {
                return false;
            }
            Attachment.MonetarySystemExchange attachment = (Attachment.MonetarySystemExchange)transaction.getAttachment();
            return currencyId == 0 || attachment.getCurrencyId() == currencyId;
        };

        List<? extends Transaction> transactions = Eagle.getBlockchain().getExpectedTransactions(filter);

        JSONArray exchangeRequests = new JSONArray();
        transactions.forEach(transaction -> exchangeRequests.add(JSONData.expectedExchangeRequest(transaction, includeCurrencyInfo)));
        JSONObject response = new JSONObject();
        response.put("exchangeRequests", exchangeRequests);
        return response;

    }

}
