
package egl.http;

import egl.EagleException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAsset extends APIServlet.APIRequestHandler {

    static final GetAsset instance = new GetAsset();

    private GetAsset() {
        super(new APITag[] {APITag.AE}, "asset", "includeCounts");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        return JSONData.asset(ParameterParser.getAsset(req), includeCounts);
    }

}
