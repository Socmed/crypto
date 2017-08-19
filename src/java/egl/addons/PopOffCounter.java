
package egl.addons;

import egl.BlockchainProcessor;
import egl.Eagle;
import egl.EagleException;
import egl.http.APIServlet;
import egl.http.APITag;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class PopOffCounter implements AddOn {

    private volatile int numberOfPopOffs = 0;

    @Override
    public void init() {
        Eagle.getBlockchainProcessor().addListener(block -> numberOfPopOffs += 1, BlockchainProcessor.Event.BLOCK_POPPED);
    }

    @Override
    public APIServlet.APIRequestHandler getAPIRequestHandler() {
        return new APIServlet.APIRequestHandler(new APITag[]{APITag.ADDONS, APITag.BLOCKS}) {
            @Override
            protected JSONStreamAware processRequest(HttpServletRequest request) throws EagleException {
                JSONObject response = new JSONObject();
                response.put("numberOfPopOffs", numberOfPopOffs);
                return response;
            }
            @Override
            protected boolean allowRequiredBlockParameters() {
                return false;
            }
        };
    }

    @Override
    public String getAPIRequestType() {
        return "getNumberOfPopOffs";
    }

}
