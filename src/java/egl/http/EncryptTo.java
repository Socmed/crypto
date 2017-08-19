
package egl.http;

import egl.Account;
import egl.EagleException;
import egl.crypto.EncryptedData;
import egl.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.INCORRECT_MESSAGE_TO_ENCRYPT;
import static egl.http.JSONResponses.INCORRECT_RECIPIENT;
import static egl.http.JSONResponses.MISSING_MESSAGE_TO_ENCRYPT;

public final class EncryptTo extends APIServlet.APIRequestHandler {

    static final EncryptTo instance = new EncryptTo();

    private EncryptTo() {
        super(new APITag[] {APITag.MESSAGES}, "recipient", "messageToEncrypt", "messageToEncryptIsText", "compressMessageToEncrypt", "secretPhrase");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long recipientId = ParameterParser.getAccountId(req, "recipient", true);
        byte[] recipientPublicKey = Account.getPublicKey(recipientId);
        if (recipientPublicKey == null) {
            return INCORRECT_RECIPIENT;
        }
        boolean isText = !"false".equalsIgnoreCase(req.getParameter("messageToEncryptIsText"));
        boolean compress = !"false".equalsIgnoreCase(req.getParameter("compressMessageToEncrypt"));
        String plainMessage = Convert.emptyToNull(req.getParameter("messageToEncrypt"));
        if (plainMessage == null) {
            return MISSING_MESSAGE_TO_ENCRYPT;
        }
        byte[] plainMessageBytes;
        try {
            plainMessageBytes = isText ? Convert.toBytes(plainMessage) : Convert.parseHexString(plainMessage);
        } catch (RuntimeException e) {
            return INCORRECT_MESSAGE_TO_ENCRYPT;
        }
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        EncryptedData encryptedData = Account.encryptTo(recipientPublicKey, plainMessageBytes, secretPhrase, compress);
        return JSONData.encryptedData(encryptedData);

    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}
