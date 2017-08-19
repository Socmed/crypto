
package egl.http;

import egl.EagleException;
import egl.TaggedData;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDataTagCount extends APIServlet.APIRequestHandler {

    static final GetDataTagCount instance = new GetDataTagCount();

    private GetDataTagCount() {
        super(new APITag[] {APITag.DATA});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        JSONObject response = new JSONObject();
        response.put("numberOfDataTags", TaggedData.Tag.getTagCount());
        return response;
    }

}
