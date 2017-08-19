
package egl.http;

import egl.Eagle;
import egl.EagleException;
import egl.Poll;
import egl.Vote;
import egl.VoteWeighting;
import egl.util.JSON;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetPollVote extends APIServlet.APIRequestHandler  {
    static final GetPollVote instance = new GetPollVote();

    private GetPollVote() {
        super(new APITag[] {APITag.VS}, "poll", "account", "includeWeights");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Poll poll = ParameterParser.getPoll(req);
        long accountId = ParameterParser.getAccountId(req, true);
        boolean includeWeights = "true".equalsIgnoreCase(req.getParameter("includeWeights"));
        Vote vote = Vote.getVote(poll.getId(), accountId);
        if (vote != null) {
            int countHeight;
            JSONData.VoteWeighter weighter = null;
            if (includeWeights && (countHeight = Math.min(poll.getFinishHeight(), Eagle.getBlockchain().getHeight()))
                    >= Eagle.getBlockchainProcessor().getMinRollbackHeight()) {
                VoteWeighting voteWeighting = poll.getVoteWeighting();
                VoteWeighting.VotingModel votingModel = voteWeighting.getVotingModel();
                weighter = voterId -> votingModel.calcWeight(voteWeighting, voterId, countHeight);
            }
            return JSONData.vote(vote, weighter);
        }
        return JSON.emptyJSON;
    }
}
