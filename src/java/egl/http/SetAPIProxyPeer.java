
package egl.http;

import egl.EagleException;
import egl.peer.Peer;
import egl.peer.Peers;
import egl.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.API_PROXY_NO_OPEN_API_PEERS;
import static egl.http.JSONResponses.PEER_NOT_CONNECTED;
import static egl.http.JSONResponses.PEER_NOT_OPEN_API;
import static egl.http.JSONResponses.UNKNOWN_PEER;

public class SetAPIProxyPeer extends APIServlet.APIRequestHandler {

    static final SetAPIProxyPeer instance = new SetAPIProxyPeer();

    private SetAPIProxyPeer() {
        super(new APITag[] {APITag.NETWORK}, "peer");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws EagleException {
        String peerAddress = Convert.emptyToNull(request.getParameter("peer"));
        if (peerAddress == null) {
            Peer peer = APIProxy.getInstance().setForcedPeer(null);
            if (peer == null) {
                return API_PROXY_NO_OPEN_API_PEERS;
            }
            return JSONData.peer(peer);
        }
        Peer peer = Peers.findOrCreatePeer(peerAddress, false);
        if (peer == null) {
            return UNKNOWN_PEER;
        }
        if (peer.getState() != Peer.State.CONNECTED ) {
            return PEER_NOT_CONNECTED;
        }
        if (!peer.isOpenAPI()) {
            return PEER_NOT_OPEN_API;
        }
        APIProxy.getInstance().setForcedPeer(peer);
        return JSONData.peer(peer);
    }

    @Override
    protected boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }


}
