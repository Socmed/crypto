
package egl.http;

import egl.Transaction;
import egl.TransactionScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public final class GetScheduledTransactions extends APIServlet.APIRequestHandler {

    static final GetScheduledTransactions instance = new GetScheduledTransactions();

    private GetScheduledTransactions() {
        super(new APITag[] {APITag.TRANSACTIONS, APITag.ACCOUNTS}, "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        long accountId = ParameterParser.getAccountId(req, true);
        JSONArray jsonArray = new JSONArray();
        List<Transaction> transactions = TransactionScheduler.getScheduledTransactions(accountId);
        for (Transaction transaction : transactions) {
            jsonArray.add(JSONData.unconfirmedTransaction(transaction));
        }
        JSONObject response = new JSONObject();
        response.put("scheduledTransactions", jsonArray);
        return response;
    }

    @Override
    protected boolean requireFullClient() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}
