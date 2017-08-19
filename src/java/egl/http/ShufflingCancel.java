
package egl.http;

import egl.Account;
import egl.Attachment;
import egl.EagleException;
import egl.Shuffling;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class ShufflingCancel extends CreateTransaction {

    static final ShufflingCancel instance = new ShufflingCancel();

    private ShufflingCancel() {
        super(new APITag[] {APITag.SHUFFLING, APITag.CREATE_TRANSACTION}, "shuffling", "cancellingAccount", "shufflingStateHash");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Shuffling shuffling = ParameterParser.getShuffling(req);
        long cancellingAccountId = ParameterParser.getAccountId(req, "cancellingAccount", false);
        byte[] shufflingStateHash = ParameterParser.getBytes(req, "shufflingStateHash", true);
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        Attachment.ShufflingCancellation attachment = shuffling.revealKeySeeds(secretPhrase, cancellingAccountId, shufflingStateHash);
        Account account = ParameterParser.getSenderAccount(req);
        return createTransaction(req, account, attachment);
    }
}
