
package egl.http;

import egl.Account;
import egl.Attachment;
import egl.Constants;
import egl.Currency;
import egl.EagleException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Increase the value of currency units by paying EGL
 * <p>
 * Parameters
 * <ul>
 * <li>currency - currency id
 * <li>amountPerUnitNQT - the NQT amount invested into increasing the value of a single currency unit.<br>
 * This value is multiplied by the currency total supply and the result is deducted from the sender's account balance.
 * </ul>
 * <p>
 * Constraints
 * <p>
 * This API is allowed only when the currency is {@link egl.CurrencyType#RESERVABLE} and is not yet active.
 * <p>
 * The sender account is registered as a founder. Once the currency becomes active
 * the total supply is distributed between the founders based on their proportional investment<br>
 * The list of founders and their NQT investment can be obtained using the {@link egl.http.GetCurrencyFounders} API.
 */

public final class CurrencyReserveIncrease extends CreateTransaction {

    static final CurrencyReserveIncrease instance = new CurrencyReserveIncrease();

    private CurrencyReserveIncrease() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "currency", "amountPerUnitNQT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Currency currency = ParameterParser.getCurrency(req);
        long amountPerUnitNQT = ParameterParser.getLong(req, "amountPerUnitNQT", 1L, Constants.MAX_BALANCE_NQT, true);
        Account account = ParameterParser.getSenderAccount(req);
        Attachment attachment = new Attachment.MonetarySystemReserveIncrease(currency.getId(), amountPerUnitNQT);
        return createTransaction(req, account, attachment);

    }

}
