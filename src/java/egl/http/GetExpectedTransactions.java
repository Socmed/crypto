
package egl.http;

import egl.Eagle;
import egl.EagleException;
import egl.Transaction;
import egl.util.Convert;
import egl.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

public final class GetExpectedTransactions extends APIServlet.APIRequestHandler {

    static final GetExpectedTransactions instance = new GetExpectedTransactions();

    private GetExpectedTransactions() {
        super(new APITag[] {APITag.TRANSACTIONS}, "account", "account", "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        Set<Long> accountIds = Convert.toSet(ParameterParser.getAccountIds(req, false));
        Filter<Transaction> filter = accountIds.isEmpty() ? transaction -> true :
                transaction -> accountIds.contains(transaction.getSenderId()) || accountIds.contains(transaction.getRecipientId());

        List<? extends Transaction> transactions = Eagle.getBlockchain().getExpectedTransactions(filter);

        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        transactions.forEach(transaction -> jsonArray.add(JSONData.unconfirmedTransaction(transaction)));
        response.put("expectedTransactions", jsonArray);

        return response;
    }

}
