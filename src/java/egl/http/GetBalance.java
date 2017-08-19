
package egl.http;

import egl.Account;
import egl.Eagle;
import egl.EagleException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetBalance extends APIServlet.APIRequestHandler {

    static final GetBalance instance = new GetBalance();

    private GetBalance() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "includeEffectiveBalance", "height");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        boolean includeEffectiveBalance = "true".equalsIgnoreCase(req.getParameter("includeEffectiveBalance"));
        long accountId = ParameterParser.getAccountId(req, true);
        int height = ParameterParser.getHeight(req);
        if (height < 0) {
            height = Eagle.getBlockchain().getHeight();
        }
        Account account = Account.getAccount(accountId, height);
        return JSONData.accountBalance(account, includeEffectiveBalance, height);
    }

}
