
package egl.http;

import egl.DigitalGoodsStore;
import egl.EagleException;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSGoodsCount extends APIServlet.APIRequestHandler {

    static final GetDGSGoodsCount instance = new GetDGSGoodsCount();

    private GetDGSGoodsCount() {
        super(new APITag[] {APITag.DGS}, "seller", "inStockOnly");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        long sellerId = ParameterParser.getAccountId(req, "seller", false);
        boolean inStockOnly = !"false".equalsIgnoreCase(req.getParameter("inStockOnly"));

        JSONObject response = new JSONObject();
        response.put("numberOfGoods", sellerId != 0
                ? DigitalGoodsStore.Goods.getSellerGoodsCount(sellerId, inStockOnly)
                : inStockOnly ? DigitalGoodsStore.Goods.getCountInStock() : DigitalGoodsStore.Goods.getCount());
        return response;
    }

}
