
package egl.http;

import egl.Eagle;
import egl.EagleException;
import egl.Poll;
import egl.Vote;
import egl.VoteWeighting;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetPollVotes extends APIServlet.APIRequestHandler  {
    static final GetPollVotes instance = new GetPollVotes();

    private GetPollVotes() {
        super(new APITag[] {APITag.VS}, "poll", "firstIndex", "lastIndex", "includeWeights");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeWeights = "true".equalsIgnoreCase(req.getParameter("includeWeights"));
        Poll poll = ParameterParser.getPoll(req);
        int countHeight;
        JSONData.VoteWeighter weighter = null;
        if (includeWeights && (countHeight = Math.min(poll.getFinishHeight(), Eagle.getBlockchain().getHeight()))
                >= Eagle.getBlockchainProcessor().getMinRollbackHeight()) {
            VoteWeighting voteWeighting = poll.getVoteWeighting();
            VoteWeighting.VotingModel votingModel = voteWeighting.getVotingModel();
            weighter = voterId -> votingModel.calcWeight(voteWeighting, voterId, countHeight);
        }
        JSONArray votesJson = new JSONArray();
        try (DbIterator<Vote> votes = Vote.getVotes(poll.getId(), firstIndex, lastIndex)) {
            for (Vote vote : votes) {
                votesJson.add(JSONData.vote(vote, weighter));
            }
        }
        JSONObject response = new JSONObject();
        response.put("votes", votesJson);
        return response;
    }
}
