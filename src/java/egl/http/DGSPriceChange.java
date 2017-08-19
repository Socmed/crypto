
package egl.http;

import egl.Account;
import egl.Attachment;
import egl.DigitalGoodsStore;
import egl.EagleException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.UNKNOWN_GOODS;

public final class DGSPriceChange extends CreateTransaction {

    static final DGSPriceChange instance = new DGSPriceChange();

    private DGSPriceChange() {
        super(new APITag[] {APITag.DGS, APITag.CREATE_TRANSACTION},
                "goods", "priceNQT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Account account = ParameterParser.getSenderAccount(req);
        DigitalGoodsStore.Goods goods = ParameterParser.getGoods(req);
        long priceNQT = ParameterParser.getPriceNQT(req);
        if (goods.isDelisted() || goods.getSellerId() != account.getId()) {
            return UNKNOWN_GOODS;
        }
        Attachment attachment = new Attachment.DigitalGoodsPriceChange(goods.getId(), priceNQT);
        return createTransaction(req, account, attachment);
    }

}
