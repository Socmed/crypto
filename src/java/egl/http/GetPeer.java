
package egl.http;

import egl.peer.Peer;
import egl.peer.Peers;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.MISSING_PEER;
import static egl.http.JSONResponses.UNKNOWN_PEER;

public final class GetPeer extends APIServlet.APIRequestHandler {

    static final GetPeer instance = new GetPeer();

    private GetPeer() {
        super(new APITag[] {APITag.NETWORK}, "peer");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        String peerAddress = req.getParameter("peer");
        if (peerAddress == null) {
            return MISSING_PEER;
        }

        Peer peer = Peers.findOrCreatePeer(peerAddress, false);
        if (peer == null) {
            return UNKNOWN_PEER;
        }

        return JSONData.peer(peer);

    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}
