
package egl.http;

import egl.Account;
import egl.Attachment;
import egl.Currency;
import egl.EagleException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.NOT_ENOUGH_CURRENCY;

public final class TransferCurrency extends CreateTransaction {

    static final TransferCurrency instance = new TransferCurrency();

    private TransferCurrency() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "recipient", "currency", "units");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long recipient = ParameterParser.getAccountId(req, "recipient", true);

        Currency currency = ParameterParser.getCurrency(req);
        long units = ParameterParser.getLong(req, "units", 0, Long.MAX_VALUE, true);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new Attachment.MonetarySystemCurrencyTransfer(currency.getId(), units);
        try {
            return createTransaction(req, account, recipient, 0, attachment);
        } catch (EagleException.InsufficientBalanceException e) {
            return NOT_ENOUGH_CURRENCY;
        }
    }

}
