
package egl.http;

import egl.Eagle;
import egl.EagleException;
import egl.Transaction;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetReferencingTransactions extends APIServlet.APIRequestHandler {

    static final GetReferencingTransactions instance = new GetReferencingTransactions();

    private GetReferencingTransactions() {
        super(new APITag[] {APITag.TRANSACTIONS}, "transaction", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long transactionId = ParameterParser.getUnsignedLong(req, "transaction", true);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray transactions = new JSONArray();
        try (DbIterator<? extends Transaction> iterator = Eagle.getBlockchain().getReferencingTransactions(transactionId, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                transactions.add(JSONData.transaction(transaction));
            }
        }

        JSONObject response = new JSONObject();
        response.put("transactions", transactions);
        return response;

    }

}
