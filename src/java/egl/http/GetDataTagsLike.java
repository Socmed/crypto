
package egl.http;

import egl.EagleException;
import egl.TaggedData;
import egl.db.DbIterator;
import egl.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDataTagsLike extends APIServlet.APIRequestHandler {

    static final GetDataTagsLike instance = new GetDataTagsLike();

    private GetDataTagsLike() {
        super(new APITag[] {APITag.DATA, APITag.SEARCH}, "tagPrefix", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        String prefix = Convert.emptyToNull(req.getParameter("tagPrefix"));
        if (prefix == null) {
            return JSONResponses.missing("tagPrefix");
        }
        if (prefix.length() < 2) {
            return JSONResponses.incorrect("tagPrefix", "tagPrefix must be at least 2 characters long");
        }

        JSONObject response = new JSONObject();
        JSONArray tagsJSON = new JSONArray();
        response.put("tags", tagsJSON);
        try (DbIterator<TaggedData.Tag> tags = TaggedData.Tag.getTagsLike(prefix, firstIndex, lastIndex)) {
            while (tags.hasNext()) {
                tagsJSON.add(JSONData.dataTag(tags.next()));
            }
        }
        return response;
    }

}
