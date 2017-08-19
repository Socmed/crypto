
package egl.http;

import egl.EagleException;
import egl.TaggedData;
import egl.db.DbIterator;
import egl.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class SearchTaggedData extends APIServlet.APIRequestHandler {

    static final SearchTaggedData instance = new SearchTaggedData();

    private SearchTaggedData() {
        super(new APITag[] {APITag.DATA, APITag.SEARCH}, "query", "tag", "channel", "account", "firstIndex", "lastIndex", "includeData");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        long accountId = ParameterParser.getAccountId(req, "account", false);
        String query = ParameterParser.getSearchQuery(req);
        String channel = Convert.emptyToNull(req.getParameter("channel"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeData = "true".equalsIgnoreCase(req.getParameter("includeData"));

        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        response.put("data", jsonArray);
        try (DbIterator<TaggedData> data = TaggedData.searchData(query, channel, accountId, firstIndex, lastIndex)) {
            while (data.hasNext()) {
                jsonArray.add(JSONData.taggedData(data.next(), includeData));
            }
        }

        return response;
    }

}
