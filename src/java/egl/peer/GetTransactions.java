
package egl.peer;

import egl.Blockchain;
import egl.Constants;
import egl.Eagle;
import egl.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

/**
 * Get the transactions
 */
public class GetTransactions extends PeerServlet.PeerRequestHandler {

    static final GetTransactions instance = new GetTransactions();

    private GetTransactions() {}

    @Override
    JSONStreamAware processRequest(JSONObject request, Peer peer) {
        if (!Constants.INCLUDE_EXPIRED_PRUNABLE) {
            return PeerServlet.UNSUPPORTED_REQUEST_TYPE;
        }
        JSONObject response = new JSONObject();
        JSONArray transactionArray = new JSONArray();
        JSONArray transactionIds = (JSONArray)request.get("transactionIds");
        Blockchain blockchain = Eagle.getBlockchain();
        //
        // Return the transactions to the caller
        //
        if (transactionIds != null) {
            transactionIds.forEach(transactionId -> {
                long id = Long.parseUnsignedLong((String)transactionId);
                Transaction transaction = blockchain.getTransaction(id);
                if (transaction != null) {
                    transaction.getAppendages(true);
                    JSONObject transactionJSON = transaction.getJSONObject();
                    transactionArray.add(transactionJSON);
                }
            });
        }
        response.put("transactions", transactionArray);
        return response;
    }

    @Override
    boolean rejectWhileDownloading() {
        return true;
    }
}
