
package egl.http;

import egl.Block;
import egl.Eagle;
import egl.EagleException;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountBlocks extends APIServlet.APIRequestHandler {

    static final GetAccountBlocks instance = new GetAccountBlocks();

    private GetAccountBlocks() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "timestamp", "firstIndex", "lastIndex", "includeTransactions");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long accountId = ParameterParser.getAccountId(req, true);
        int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        boolean includeTransactions = "true".equalsIgnoreCase(req.getParameter("includeTransactions"));

        JSONArray blocks = new JSONArray();
        try (DbIterator<? extends Block> iterator = Eagle.getBlockchain().getBlocks(accountId, timestamp, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Block block = iterator.next();
                blocks.add(JSONData.block(block, includeTransactions, false));
            }
        }

        JSONObject response = new JSONObject();
        response.put("blocks", blocks);

        return response;
    }

}
