
package egl.http;

import egl.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class FullHashToId extends APIServlet.APIRequestHandler {

    static final FullHashToId instance = new FullHashToId();

    private FullHashToId() {
        super(new APITag[] {APITag.UTILS}, "fullHash");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        long longId = Convert.fullHashToId(Convert.parseHexString(req.getParameter("fullHash")));
        response.put("longId", String.valueOf(longId));
        response.put("stringId", Long.toUnsignedString(longId));
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

}
