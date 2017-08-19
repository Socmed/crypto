
package egl.http;

import egl.DigitalGoodsStore;
import egl.EagleException;
import egl.db.DbIterator;
import egl.db.DbUtils;
import egl.db.FilteringIterator;
import egl.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class SearchDGSGoods extends APIServlet.APIRequestHandler {

    static final SearchDGSGoods instance = new SearchDGSGoods();

    private SearchDGSGoods() {
        super(new APITag[] {APITag.DGS, APITag.SEARCH}, "query", "tag", "seller", "firstIndex", "lastIndex", "inStockOnly", "hideDelisted", "includeCounts");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        long sellerId = ParameterParser.getAccountId(req, "seller", false);
        String query = ParameterParser.getSearchQuery(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean inStockOnly = !"false".equalsIgnoreCase(req.getParameter("inStockOnly"));
        boolean hideDelisted = "true".equalsIgnoreCase(req.getParameter("hideDelisted"));
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));

        JSONObject response = new JSONObject();
        JSONArray goodsJSON = new JSONArray();
        response.put("goods", goodsJSON);

        Filter<DigitalGoodsStore.Goods> filter = hideDelisted ? goods -> ! goods.isDelisted() : goods -> true;

        FilteringIterator<DigitalGoodsStore.Goods> iterator = null;
        try {
            DbIterator<DigitalGoodsStore.Goods> goods;
            if (sellerId == 0) {
                goods = DigitalGoodsStore.Goods.searchGoods(query, inStockOnly, 0, -1);
            } else {
                goods = DigitalGoodsStore.Goods.searchSellerGoods(query, sellerId, inStockOnly, 0, -1);
            }
            iterator = new FilteringIterator<>(goods, filter, firstIndex, lastIndex);
            while (iterator.hasNext()) {
                DigitalGoodsStore.Goods good = iterator.next();
                goodsJSON.add(JSONData.goods(good, includeCounts));
            }
        } finally {
            DbUtils.close(iterator);
        }

        return response;
    }

}
