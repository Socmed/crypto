
package egl.http;

import egl.EagleException;
import egl.PhasingPoll;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetAccountPhasedTransactionCount extends APIServlet.APIRequestHandler {
    static final GetAccountPhasedTransactionCount instance = new GetAccountPhasedTransactionCount();

    private GetAccountPhasedTransactionCount() {
        super(new APITag[]{APITag.ACCOUNTS, APITag.PHASING}, "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        long accountId = ParameterParser.getAccountId(req, true);
        JSONObject response = new JSONObject();
        response.put("numberOfPhasedTransactions", PhasingPoll.getAccountPhasedTransactionCount(accountId));
        return response;
    }
}