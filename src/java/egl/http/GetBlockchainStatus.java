
package egl.http;

import egl.AccountLedger;
import egl.Block;
import egl.BlockchainProcessor;
import egl.Constants;
import egl.Eagle;
import egl.peer.Peer;
import egl.peer.Peers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;

public final class GetBlockchainStatus extends APIServlet.APIRequestHandler {

    static final GetBlockchainStatus instance = new GetBlockchainStatus();

    private GetBlockchainStatus() {
        super(new APITag[] {APITag.BLOCKS, APITag.INFO});
    }

    @Override
    protected JSONObject processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        response.put("application", Eagle.APPLICATION);
        response.put("version", Eagle.VERSION);
        response.put("time", Eagle.getEpochTime());
        Block lastBlock = Eagle.getBlockchain().getLastBlock();
        response.put("lastBlock", lastBlock.getStringId());
        response.put("cumulativeDifficulty", lastBlock.getCumulativeDifficulty().toString());
        response.put("numberOfBlocks", lastBlock.getHeight() + 1);
        BlockchainProcessor blockchainProcessor = Eagle.getBlockchainProcessor();
        Peer lastBlockchainFeeder = blockchainProcessor.getLastBlockchainFeeder();
        response.put("lastBlockchainFeeder", lastBlockchainFeeder == null ? null : lastBlockchainFeeder.getAnnouncedAddress());
        response.put("lastBlockchainFeederHeight", blockchainProcessor.getLastBlockchainFeederHeight());
        response.put("isScanning", blockchainProcessor.isScanning());
        response.put("isDownloading", blockchainProcessor.isDownloading());
        response.put("maxRollback", Constants.MAX_ROLLBACK);
        response.put("currentMinRollbackHeight", Eagle.getBlockchainProcessor().getMinRollbackHeight());
        response.put("isTestnet", Constants.isTestnet);
        response.put("maxPrunableLifetime", Constants.MAX_PRUNABLE_LIFETIME);
        response.put("includeExpiredPrunable", Constants.INCLUDE_EXPIRED_PRUNABLE);
        response.put("correctInvalidFees", Constants.correctInvalidFees);
        response.put("ledgerTrimKeep", AccountLedger.trimKeep);
        JSONArray servicesArray = new JSONArray();
        Peers.getServices().forEach(service -> servicesArray.add(service.name()));
        response.put("services", servicesArray);
        if (APIProxy.isActivated()) {
            String servingPeer = APIProxy.getInstance().getMainPeerAnnouncedAddress();
            response.put("apiProxy", true);
            response.put("apiProxyPeer", servingPeer);
        } else {
            response.put("apiProxy", false);
        }
        response.put("isLightClient", Constants.isLightClient);
        response.put("maxAPIRecords", API.maxRecords);
        response.put("blockchainState", Peers.getMyBlockchainState());
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}
