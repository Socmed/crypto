
package egl.http;

import egl.EagleException;
import egl.TaggedData;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAllTaggedData extends APIServlet.APIRequestHandler {

    static final GetAllTaggedData instance = new GetAllTaggedData();

    private GetAllTaggedData() {
        super(new APITag[] {APITag.DATA}, "firstIndex", "lastIndex", "includeData");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeData = "true".equalsIgnoreCase(req.getParameter("includeData"));

        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        response.put("data", jsonArray);

        try (DbIterator<TaggedData> data = TaggedData.getAll(firstIndex, lastIndex)) {
            while (data.hasNext()) {
                jsonArray.add(JSONData.taggedData(data.next(), includeData));
            }
        }
        return response;
    }

}
