
package egl.http;

import egl.AssetDividend;
import egl.EagleException;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssetDividends extends APIServlet.APIRequestHandler {

    static final GetAssetDividends instance = new GetAssetDividends();

    private GetAssetDividends() {
        super(new APITag[] {APITag.AE}, "asset", "firstIndex", "lastIndex", "timestamp");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        JSONArray dividendsData = new JSONArray();
        try (DbIterator<AssetDividend> dividends = AssetDividend.getAssetDividends(assetId, firstIndex, lastIndex)) {
            while (dividends.hasNext()) {
                AssetDividend assetDividend = dividends.next();
                if (assetDividend.getTimestamp() < timestamp) {
                    break;
                }
                dividendsData.add(JSONData.assetDividend(assetDividend));
            }
        }
        response.put("dividends", dividendsData);
        return response;
    }

}
