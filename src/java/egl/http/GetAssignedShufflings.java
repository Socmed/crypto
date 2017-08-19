
package egl.http;

import egl.Shuffling;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssignedShufflings extends APIServlet.APIRequestHandler {

    static final GetAssignedShufflings instance = new GetAssignedShufflings();

    private GetAssignedShufflings() {
        super(new APITag[] {APITag.SHUFFLING}, "account", "includeHoldingInfo", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        long accountId = ParameterParser.getAccountId(req, "account", true);
        boolean includeHoldingInfo = "true".equalsIgnoreCase(req.getParameter("includeHoldingInfo"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        response.put("shufflings", jsonArray);
        try (DbIterator<Shuffling> shufflings = Shuffling.getAssignedShufflings(accountId, firstIndex, lastIndex)) {
            for (Shuffling shuffling : shufflings) {
                jsonArray.add(JSONData.shuffling(shuffling, includeHoldingInfo));
            }
        }
        return response;
    }

}
