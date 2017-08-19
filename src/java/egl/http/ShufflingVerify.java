
package egl.http;

import egl.Account;
import egl.Attachment;
import egl.EagleException;
import egl.Shuffling;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public final class ShufflingVerify extends CreateTransaction {

    static final ShufflingVerify instance = new ShufflingVerify();

    private ShufflingVerify() {
        super(new APITag[] {APITag.SHUFFLING, APITag.CREATE_TRANSACTION}, "shuffling", "shufflingStateHash");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Shuffling shuffling = ParameterParser.getShuffling(req);
        byte[] shufflingStateHash = ParameterParser.getBytes(req, "shufflingStateHash", true);
        if (!Arrays.equals(shufflingStateHash, shuffling.getStateHash())) {
            return JSONResponses.incorrect("shufflingStateHash", "Shuffling is in a different state now");
        }
        Attachment attachment = new Attachment.ShufflingVerification(shuffling.getId(), shufflingStateHash);

        Account account = ParameterParser.getSenderAccount(req);
        return createTransaction(req, account, attachment);
    }
}
