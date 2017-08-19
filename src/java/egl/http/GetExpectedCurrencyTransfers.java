
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

public final class GetExpectedCurrencyTransfers extends APIServlet.APIRequestHandler {

    static final GetExpectedCurrencyTransfers instance = new GetExpectedCurrencyTransfers();

    private GetExpectedCurrencyTransfers() {
        super(new APITag[]{APITag.MS}, "currency", "account", "includeCurrencyInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long currencyId = ParameterParser.getUnsignedLong(req, "currency", false);
        long accountId = ParameterParser.getAccountId(req, "account", false);
        boolean includeCurrencyInfo = "true".equalsIgnoreCase(req.getParameter("includeCurrencyInfo"));

        Filter<Transaction> filter = transaction -> {
            if (transaction.getType() != MonetarySystem.CURRENCY_TRANSFER) {
                return false;
            }
            if (accountId != 0 && transaction.getSenderId() != accountId && transaction.getRecipientId() != accountId) {
                return false;
            }
            Attachment.MonetarySystemCurrencyTransfer attachment = (Attachment.MonetarySystemCurrencyTransfer)transaction.getAttachment();
            return currencyId == 0 || attachment.getCurrencyId() == currencyId;
        };

        List<? extends Transaction> transactions = Eagle.getBlockchain().getExpectedTransactions(filter);

        JSONObject response = new JSONObject();
        JSONArray transfersData = new JSONArray();
        transactions.forEach(transaction -> transfersData.add(JSONData.expectedCurrencyTransfer(transaction, includeCurrencyInfo)));
        response.put("transfers", transfersData);

        return response;
    }

}
