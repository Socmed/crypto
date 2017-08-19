
package egl.http;

import egl.EagleException;
import egl.Shuffler;
import egl.Shuffling;
import egl.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class StartShuffler extends APIServlet.APIRequestHandler {

    static final StartShuffler instance = new StartShuffler();

    private StartShuffler() {
        super(new APITag[]{APITag.SHUFFLING}, "secretPhrase", "shufflingFullHash", "recipientSecretPhrase", "recipientPublicKey");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        byte[] shufflingFullHash = ParameterParser.getBytes(req, "shufflingFullHash", true);
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        byte[] recipientPublicKey = ParameterParser.getPublicKey(req, "recipient");
        try {
            Shuffler shuffler = Shuffler.addOrGetShuffler(secretPhrase, recipientPublicKey, shufflingFullHash);
            return shuffler != null ? JSONData.shuffler(shuffler, false) : JSON.emptyJSON;
        } catch (Shuffler.ShufflerLimitException e) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 7);
            response.put("errorDescription", e.getMessage());
            return JSON.prepare(response);
        } catch (Shuffler.DuplicateShufflerException e) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 8);
            response.put("errorDescription", e.getMessage());
            return JSON.prepare(response);
        } catch (Shuffler.InvalidRecipientException e) {
            return JSONResponses.incorrect("recipientPublicKey", e.getMessage());
        } catch (Shuffler.ControlledAccountException e) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 9);
            response.put("errorDescription", e.getMessage());
            return JSON.prepare(response);
        } catch (Shuffler.ShufflerException e) {
            if (e.getCause() instanceof EagleException.InsufficientBalanceException) {
                Shuffling shuffling = Shuffling.getShuffling(shufflingFullHash);
                if (shuffling == null) {
                    return JSONResponses.NOT_ENOUGH_FUNDS;
                }
                return JSONResponses.notEnoughHolding(shuffling.getHoldingType());
            }
            JSONObject response = new JSONObject();
            response.put("errorCode", 10);
            response.put("errorDescription", e.getMessage());
            return JSON.prepare(response);
        }
    }

    @Override
    protected boolean requirePost() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireFullClient() {
        return true;
    }

}
