
package egl.http;

import egl.EagleException;
import egl.PhasingPoll;
import egl.PhasingVote;
import egl.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetPhasingPollVotes extends APIServlet.APIRequestHandler  {
    static final GetPhasingPollVotes instance = new GetPhasingPollVotes();

    private GetPhasingPollVotes() {
        super(new APITag[] {APITag.PHASING}, "transaction", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        long transactionId = ParameterParser.getUnsignedLong(req, "transaction", true);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        PhasingPoll phasingPoll = PhasingPoll.getPoll(transactionId);
        if (phasingPoll != null) {
            JSONObject response = new JSONObject();
            JSONArray votesJSON = new JSONArray();
            try (DbIterator<PhasingVote> votes = PhasingVote.getVotes(transactionId, firstIndex, lastIndex)) {
                for (PhasingVote vote : votes) {
                    votesJSON.add(JSONData.phasingPollVote(vote));
                }
            }
            response.put("votes", votesJSON);
            return response;
        }
        return JSONResponses.UNKNOWN_TRANSACTION;
    }
}
