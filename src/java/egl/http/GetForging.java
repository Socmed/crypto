
package egl.http;

import egl.Account;
import egl.Generator;
import egl.Eagle;
import egl.crypto.Crypto;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.NOT_FORGING;
import static egl.http.JSONResponses.UNKNOWN_ACCOUNT;


public final class GetForging extends APIServlet.APIRequestHandler {

    static final GetForging instance = new GetForging();

    private GetForging() {
        super(new APITag[] {APITag.FORGING}, "secretPhrase", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        int elapsedTime = Eagle.getEpochTime() - Eagle.getBlockchain().getLastBlock().getTimestamp();
        if (secretPhrase != null) {
            Account account = Account.getAccount(Crypto.getPublicKey(secretPhrase));
            if (account == null) {
                return UNKNOWN_ACCOUNT;
            }
            Generator generator = Generator.getGenerator(secretPhrase);
            if (generator == null) {
                return NOT_FORGING;
            }
            return JSONData.generator(generator, elapsedTime);
        } else {
            API.verifyPassword(req);
            JSONObject response = new JSONObject();
            JSONArray generators = new JSONArray();
            Generator.getSortedForgers().forEach(generator -> generators.add(JSONData.generator(generator, elapsedTime)));
            response.put("generators", generators);
            return response;
        }
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
