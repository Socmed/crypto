
package egl.http;

import egl.Token;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.INCORRECT_WEBSITE;
import static egl.http.JSONResponses.MISSING_TOKEN;
import static egl.http.JSONResponses.MISSING_WEBSITE;

public final class DecodeToken extends APIServlet.APIRequestHandler {

    static final DecodeToken instance = new DecodeToken();

    private DecodeToken() {
        super(new APITag[] {APITag.TOKENS}, "website", "token");
    }

    @Override
    public JSONStreamAware processRequest(HttpServletRequest req) {

        String website = req.getParameter("website");
        String tokenString = req.getParameter("token");
        if (website == null) {
            return MISSING_WEBSITE;
        } else if (tokenString == null) {
            return MISSING_TOKEN;
        }

        try {

            Token token = Token.parseToken(tokenString, website.trim());

            return JSONData.token(token);

        } catch (RuntimeException e) {
            return INCORRECT_WEBSITE;
        }
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}
