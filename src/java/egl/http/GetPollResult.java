
package egl.http;


import egl.EagleException;
import egl.Poll;
import egl.VoteWeighting;
import egl.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static egl.http.JSONResponses.POLL_RESULTS_NOT_AVAILABLE;

public class GetPollResult extends APIServlet.APIRequestHandler {

    static final GetPollResult instance = new GetPollResult();

    private GetPollResult() {
        super(new APITag[]{APITag.VS}, "poll", "votingModel", "holding", "minBalance", "minBalanceModel");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Poll poll = ParameterParser.getPoll(req);
        List<Poll.OptionResult> pollResults;
        VoteWeighting voteWeighting;
        if (Convert.emptyToNull(req.getParameter("votingModel")) == null) {
            pollResults = poll.getResults();
            voteWeighting = poll.getVoteWeighting();
        } else {
            byte votingModel = ParameterParser.getByte(req, "votingModel", (byte)0, (byte)3, true);
            long holdingId = ParameterParser.getLong(req, "holding", Long.MIN_VALUE, Long.MAX_VALUE, false);
            long minBalance = ParameterParser.getLong(req, "minBalance", 0, Long.MAX_VALUE, false);
            byte minBalanceModel = ParameterParser.getByte(req, "minBalanceModel", (byte)0, (byte)3, false);
            voteWeighting = new VoteWeighting(votingModel, holdingId, minBalance, minBalanceModel);
            voteWeighting.validate();
            pollResults = poll.getResults(voteWeighting);
        }
        if (pollResults == null) {
            return POLL_RESULTS_NOT_AVAILABLE;
        }
        return JSONData.pollResults(poll, pollResults, voteWeighting);
    }
}
