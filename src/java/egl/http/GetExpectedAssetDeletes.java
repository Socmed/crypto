
package egl.http;

import egl.Attachment;
import egl.Eagle;
import egl.EagleException;
import egl.Transaction;
import egl.TransactionType;
import egl.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public final class GetExpectedAssetDeletes extends APIServlet.APIRequestHandler {

    static final GetExpectedAssetDeletes instance = new GetExpectedAssetDeletes();

    private GetExpectedAssetDeletes() {
        super(new APITag[] {APITag.AE}, "asset", "account", "includeAssetInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        long accountId = ParameterParser.getAccountId(req, "account", false);
        boolean includeAssetInfo = "true".equalsIgnoreCase(req.getParameter("includeAssetInfo"));

        Filter<Transaction> filter = transaction -> {
            if (transaction.getType() != TransactionType.ColoredCoins.ASSET_DELETE) {
                return false;
            }
            if (accountId != 0 && transaction.getSenderId() != accountId) {
                return false;
            }
            Attachment.ColoredCoinsAssetDelete attachment = (Attachment.ColoredCoinsAssetDelete)transaction.getAttachment();
            return assetId == 0 || attachment.getAssetId() == assetId;
        };

        List<? extends Transaction> transactions = Eagle.getBlockchain().getExpectedTransactions(filter);

        JSONObject response = new JSONObject();
        JSONArray deletesData = new JSONArray();
        transactions.forEach(transaction -> deletesData.add(JSONData.expectedAssetDelete(transaction, includeAssetInfo)));
        response.put("deletes", deletesData);

        return response;
    }

}
