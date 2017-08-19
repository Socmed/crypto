
package egl.http;

import egl.AssetDelete;
import egl.EagleException;
import egl.db.DbIterator;
import egl.db.DbUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssetDeletes extends APIServlet.APIRequestHandler {

    static final GetAssetDeletes instance = new GetAssetDeletes();

    private GetAssetDeletes() {
        super(new APITag[] {APITag.AE}, "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        long accountId = ParameterParser.getAccountId(req, false);
        if (assetId == 0 && accountId == 0) {
            return JSONResponses.MISSING_ASSET_ACCOUNT;
        }
        int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeAssetInfo = "true".equalsIgnoreCase(req.getParameter("includeAssetInfo"));

        JSONObject response = new JSONObject();
        JSONArray deletesData = new JSONArray();
        DbIterator<AssetDelete> deletes = null;
        try {
            if (accountId == 0) {
                deletes = AssetDelete.getAssetDeletes(assetId, firstIndex, lastIndex);
            } else if (assetId == 0) {
                deletes = AssetDelete.getAccountAssetDeletes(accountId, firstIndex, lastIndex);
            } else {
                deletes = AssetDelete.getAccountAssetDeletes(accountId, assetId, firstIndex, lastIndex);
            }
            while (deletes.hasNext()) {
                AssetDelete assetDelete = deletes.next();
                if (assetDelete.getTimestamp() < timestamp) {
                    break;
                }
                deletesData.add(JSONData.assetDelete(assetDelete, includeAssetInfo));
            }
        } finally {
            DbUtils.close(deletes);
        }
        response.put("deletes", deletesData);

        return response;
    }

    @Override
    protected boolean startDbTransaction() {
        return true;
    }
}
