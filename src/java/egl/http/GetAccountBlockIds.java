
package egl.http;

import egl.Block;
import egl.Eagle;
import egl.EagleException;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountBlockIds extends APIServlet.APIRequestHandler {

    static final GetAccountBlockIds instance = new GetAccountBlockIds();

    private GetAccountBlockIds() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "timestamp", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long accountId = ParameterParser.getAccountId(req, true);
        int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray blockIds = new JSONArray();
        try (DbIterator<? extends Block> iterator = Eagle.getBlockchain().getBlocks(accountId, timestamp, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Block block = iterator.next();
                blockIds.add(block.getStringId());
            }
        }

        JSONObject response = new JSONObject();
        response.put("blockIds", blockIds);

        return response;
    }

}
