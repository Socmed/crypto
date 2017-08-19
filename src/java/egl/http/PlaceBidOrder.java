
package egl.http;

import egl.Account;
import egl.Asset;
import egl.Attachment;
import egl.EagleException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.NOT_ENOUGH_FUNDS;

public final class PlaceBidOrder extends CreateTransaction {

    static final PlaceBidOrder instance = new PlaceBidOrder();

    private PlaceBidOrder() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "asset", "quantityQNT", "priceNQT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        Asset asset = ParameterParser.getAsset(req);
        long priceNQT = ParameterParser.getPriceNQT(req);
        long quantityQNT = ParameterParser.getQuantityQNT(req);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new Attachment.ColoredCoinsBidOrderPlacement(asset.getId(), quantityQNT, priceNQT);
        try {
            return createTransaction(req, account, attachment);
        } catch (EagleException.InsufficientBalanceException e) {
            return NOT_ENOUGH_FUNDS;
        }
    }

}
