
package egl.http;

import egl.Eagle;
import egl.Transaction;
import egl.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.INCORRECT_TRANSACTION;
import static egl.http.JSONResponses.MISSING_TRANSACTION;
import static egl.http.JSONResponses.UNKNOWN_TRANSACTION;

public final class GetTransaction extends APIServlet.APIRequestHandler {

    static final GetTransaction instance = new GetTransaction();

    private GetTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "transaction", "fullHash", "includePhasingResult");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        String transactionIdString = Convert.emptyToNull(req.getParameter("transaction"));
        String transactionFullHash = Convert.emptyToNull(req.getParameter("fullHash"));
        if (transactionIdString == null && transactionFullHash == null) {
            return MISSING_TRANSACTION;
        }
        boolean includePhasingResult = "true".equalsIgnoreCase(req.getParameter("includePhasingResult"));

        long transactionId = 0;
        Transaction transaction;
        try {
            if (transactionIdString != null) {
                transactionId = Convert.parseUnsignedLong(transactionIdString);
                transaction = Eagle.getBlockchain().getTransaction(transactionId);
            } else {
                transaction = Eagle.getBlockchain().getTransactionByFullHash(transactionFullHash);
                if (transaction == null) {
                    return UNKNOWN_TRANSACTION;
                }
            }
        } catch (RuntimeException e) {
            return INCORRECT_TRANSACTION;
        }

        if (transaction == null) {
            transaction = Eagle.getTransactionProcessor().getUnconfirmedTransaction(transactionId);
            if (transaction == null) {
                return UNKNOWN_TRANSACTION;
            }
            return JSONData.unconfirmedTransaction(transaction);
        } else {
            return JSONData.transaction(transaction, includePhasingResult);
        }

    }

}
