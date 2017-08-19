
package egl.http;

import egl.Eagle;
import egl.EagleException;
import egl.PrunableMessage;
import egl.util.Convert;
import egl.util.Logger;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static egl.http.JSONResponses.PRUNED_TRANSACTION;

public final class DownloadPrunableMessage extends APIServlet.APIRequestHandler {

    static final DownloadPrunableMessage instance = new DownloadPrunableMessage();

    private DownloadPrunableMessage() {
        super(new APITag[] {APITag.MESSAGES}, "transaction", "secretPhrase", "sharedKey", "retrieve", "save");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request, HttpServletResponse response) throws EagleException {
        long transactionId = ParameterParser.getUnsignedLong(request, "transaction", true);
        boolean retrieve = "true".equalsIgnoreCase(request.getParameter("retrieve"));
        PrunableMessage prunableMessage = PrunableMessage.getPrunableMessage(transactionId);
        if (prunableMessage == null && retrieve) {
            if (Eagle.getBlockchainProcessor().restorePrunedTransaction(transactionId) == null) {
                return PRUNED_TRANSACTION;
            }
            prunableMessage = PrunableMessage.getPrunableMessage(transactionId);
        }
        String secretPhrase = ParameterParser.getSecretPhrase(request, false);
        byte[] sharedKey = ParameterParser.getBytes(request, "sharedKey", false);
        if (sharedKey.length != 0 && secretPhrase != null) {
            return JSONResponses.either("secretPhrase", "sharedKey");
        }
        byte[] data = null;
        if (prunableMessage != null) {
            try {
                if (secretPhrase != null) {
                    data = prunableMessage.decrypt(secretPhrase);
                } else if (sharedKey.length > 0) {
                    data = prunableMessage.decrypt(sharedKey);
                } else {
                    data = prunableMessage.getMessage();
                }
            } catch (RuntimeException e) {
                Logger.logDebugMessage("Decryption of message to recipient failed: " + e.toString());
                return JSONResponses.error("Wrong secretPhrase or sharedKey");
            }
        }
        if (data == null) {
            data = Convert.EMPTY_BYTE;
        }
        String contentDisposition = "true".equalsIgnoreCase(request.getParameter("save")) ? "attachment" : "inline";
        response.setHeader("Content-Disposition", contentDisposition + "; filename=" + Long.toUnsignedString(transactionId));
        response.setContentLength(data.length);
        try (OutputStream out = response.getOutputStream()) {
            try {
                out.write(data);
            } catch (IOException e) {
                throw new ParameterException(JSONResponses.RESPONSE_WRITE_ERROR);
            }
        } catch (IOException e) {
            throw new ParameterException(JSONResponses.RESPONSE_STREAM_ERROR);
        }
        return null;
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws EagleException {
        throw new UnsupportedOperationException();
    }
}
