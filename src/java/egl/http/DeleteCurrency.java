
package egl.http;

import egl.Account;
import egl.Attachment;
import egl.Currency;
import egl.EagleException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class DeleteCurrency extends CreateTransaction {

    static final DeleteCurrency instance = new DeleteCurrency();

    private DeleteCurrency() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "currency");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Currency currency = ParameterParser.getCurrency(req);
        Account account = ParameterParser.getSenderAccount(req);
        if (!currency.canBeDeletedBy(account.getId())) {
            return JSONResponses.CANNOT_DELETE_CURRENCY;
        }
        Attachment attachment = new Attachment.MonetarySystemCurrencyDeletion(currency.getId());
        return createTransaction(req, account, attachment);
    }
}
