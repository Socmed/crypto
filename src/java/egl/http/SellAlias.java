
package egl.http;

import egl.Account;
import egl.Alias;
import egl.Attachment;
import egl.Constants;
import egl.EagleException;
import egl.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.INCORRECT_ALIAS_OWNER;
import static egl.http.JSONResponses.INCORRECT_RECIPIENT;


public final class SellAlias extends CreateTransaction {

    static final SellAlias instance = new SellAlias();

    private SellAlias() {
        super(new APITag[] {APITag.ALIASES, APITag.CREATE_TRANSACTION}, "alias", "aliasName", "recipient", "priceNQT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Alias alias = ParameterParser.getAlias(req);
        Account owner = ParameterParser.getSenderAccount(req);

        long priceNQT = ParameterParser.getLong(req, "priceNQT", 0L, Constants.MAX_BALANCE_NQT, true);

        String recipientValue = Convert.emptyToNull(req.getParameter("recipient"));
        long recipientId = 0;
        if (recipientValue != null) {
            try {
                recipientId = Convert.parseAccountId(recipientValue);
            } catch (RuntimeException e) {
                return INCORRECT_RECIPIENT;
            }
            if (recipientId == 0) {
                return INCORRECT_RECIPIENT;
            }
        }

        if (alias.getAccountId() != owner.getId()) {
            return INCORRECT_ALIAS_OWNER;
        }

        Attachment attachment = new Attachment.MessagingAliasSell(alias.getAliasName(), priceNQT);
        return createTransaction(req, owner, recipientId, 0, attachment);
    }
}
