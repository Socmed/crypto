
package egl.http;

import egl.EagleException;
import egl.Order;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.UNKNOWN_ORDER;

public final class GetBidOrder extends APIServlet.APIRequestHandler {

    static final GetBidOrder instance = new GetBidOrder();

    private GetBidOrder() {
        super(new APITag[] {APITag.AE}, "order");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        long orderId = ParameterParser.getUnsignedLong(req, "order", true);
        Order.Bid bidOrder = Order.Bid.getBidOrder(orderId);
        if (bidOrder == null) {
            return UNKNOWN_ORDER;
        }
        return JSONData.bidOrder(bidOrder);
    }

}
