
package egl.http;

import egl.Alias;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAlias extends APIServlet.APIRequestHandler {

    static final GetAlias instance = new GetAlias();

    private GetAlias() {
        super(new APITag[] {APITag.ALIASES}, "alias", "aliasName");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        Alias alias = ParameterParser.getAlias(req);
        return JSONData.alias(alias);
    }

}
