
package egl.http;

import egl.Account;
import egl.Asset;
import egl.Attachment;
import egl.EagleException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.NOT_ENOUGH_ASSETS;

public final class TransferAsset extends CreateTransaction {

    static final TransferAsset instance = new TransferAsset();

    private TransferAsset() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "recipient", "asset", "quantityQNT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long recipient = ParameterParser.getAccountId(req, "recipient", true);

        Asset asset = ParameterParser.getAsset(req);
        long quantityQNT = ParameterParser.getQuantityQNT(req);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new Attachment.ColoredCoinsAssetTransfer(asset.getId(), quantityQNT);
        try {
            return createTransaction(req, account, recipient, 0, attachment);
        } catch (EagleException.InsufficientBalanceException e) {
            return NOT_ENOUGH_ASSETS;
        }
    }

}
