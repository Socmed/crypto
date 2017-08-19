
package egl.http;

import egl.Eagle;
import egl.Transaction;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.PRUNED_TRANSACTION;
import static egl.http.JSONResponses.UNKNOWN_TRANSACTION;

public class RetrievePrunedTransaction extends APIServlet.APIRequestHandler {

    static final RetrievePrunedTransaction instance = new RetrievePrunedTransaction();

    private RetrievePrunedTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "transaction");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long transactionId = ParameterParser.getUnsignedLong(req, "transaction", true);
        Transaction transaction = Eagle.getBlockchain().getTransaction(transactionId);
        if (transaction == null) {
            return UNKNOWN_TRANSACTION;
        }
        transaction = Eagle.getBlockchainProcessor().restorePrunedTransaction(transactionId);
        if (transaction == null) {
            return PRUNED_TRANSACTION;
        }
        return JSONData.transaction(transaction);
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected final boolean allowRequiredBlockParameters() {
        return false;
    }

}
