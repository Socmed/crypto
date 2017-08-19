
package egl.http;

import egl.EagleException;
import egl.Poll;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;


public final class GetPoll extends APIServlet.APIRequestHandler {

    static final GetPoll instance = new GetPoll();

    private GetPoll() {
        super(new APITag[] {APITag.VS}, "poll");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Poll poll = ParameterParser.getPoll(req);
        return JSONData.poll(poll);
    }
}
