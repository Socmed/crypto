
package egl.http.twophased;

import egl.BlockchainTest;
import egl.Constants;
import egl.Eagle;
import egl.VoteWeighting;
import egl.http.APICall;
import egl.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class TestCreateTwoPhased extends BlockchainTest {

    static JSONObject issueCreateTwoPhased(APICall apiCall, boolean shouldFail) {
        JSONObject twoPhased = apiCall.invoke();
        Logger.logMessage("two-phased sendMoney: " + twoPhased.toJSONString());

        generateBlock();
        String transactionId = (String)twoPhased.get("transaction");
        if (!shouldFail && transactionId == null || shouldFail && transactionId != null) {
            Assert.fail();
        }
        return twoPhased;
    }

    public static class TwoPhasedMoneyTransferBuilder extends APICall.Builder {

        public TwoPhasedMoneyTransferBuilder() {
            super("sendMoney");

            int height = Eagle.getBlockchain().getHeight();

            secretPhrase(ALICE.getSecretPhrase());
            feeNQT(2*Constants.ONE_EGL);
            recipient(BOB.getId());
            param("amountNQT", 50 * Constants.ONE_EGL);
            param("phased", "true");
            param("phasingVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode());
            param("phasingQuorum", 1);
            param("phasingWhitelisted", CHUCK.getStrId());
            param("phasingFinishHeight", height + 50);
        }

        public TwoPhasedMoneyTransferBuilder fee(long fee) {
            feeNQT(fee);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder votingModel(byte model) {
            param("phasingVotingModel", model);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder finishHeight(int maxHeight) {
            param("phasingFinishHeight", maxHeight);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder minBalance(long minBalance, byte minBalanceModel) {
            param("phasingMinBalance", minBalance);
            param("phasingMinBalanceModel", minBalanceModel);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder quorum(int quorum) {
            param("phasingQuorum", quorum);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder noWhitelist() {
            param("phasingWhitelisted", "");
            return this;
        }

        public TwoPhasedMoneyTransferBuilder whitelisted(long accountId) {
            param("phasingWhitelisted", Long.toUnsignedString(accountId));
            return this;
        }

        public TwoPhasedMoneyTransferBuilder holding(long accountId) {
            param("phasingHolding", Long.toUnsignedString(accountId));
            return this;
        }
    }


    @Test
    public void validMoneyTransfer() {
        APICall apiCall = new TwoPhasedMoneyTransferBuilder().build();
        issueCreateTwoPhased(apiCall, false);
    }

    @Test
    public void invalidMoneyTransfer() {
        int height = Eagle.getBlockchain().getHeight();

        APICall apiCall = new TwoPhasedMoneyTransferBuilder().finishHeight(height).build();
        issueCreateTwoPhased(apiCall, true);

        apiCall = new TwoPhasedMoneyTransferBuilder().finishHeight(height + 100000).build();
        issueCreateTwoPhased(apiCall, true);

        apiCall = new TwoPhasedMoneyTransferBuilder().quorum(0).build();
        issueCreateTwoPhased(apiCall, true);

        apiCall = new TwoPhasedMoneyTransferBuilder().noWhitelist().build();
        issueCreateTwoPhased(apiCall, true);

        apiCall = new TwoPhasedMoneyTransferBuilder().whitelisted(0).build();
        issueCreateTwoPhased(apiCall, true);

        apiCall = new TwoPhasedMoneyTransferBuilder().votingModel(VoteWeighting.VotingModel.ASSET.getCode()).build();
        issueCreateTwoPhased(apiCall, true);

        apiCall = new TwoPhasedMoneyTransferBuilder().votingModel(VoteWeighting.VotingModel.ASSET.getCode())
                .minBalance(50, VoteWeighting.MinBalanceModel.ASSET.getCode())
                .build();
        issueCreateTwoPhased(apiCall, true);
    }

    @Test
    public void unconfirmed() {
        List<String> transactionIds = new ArrayList<>(10);

        for(int i=0; i < 10; i++){
            APICall apiCall = new TwoPhasedMoneyTransferBuilder().build();
            JSONObject transactionJSON = issueCreateTwoPhased(apiCall, false);
            String idString = (String) transactionJSON.get("transaction");
            transactionIds.add(idString);
        }

        APICall apiCall = new TwoPhasedMoneyTransferBuilder().build();
        apiCall.invoke();

        JSONObject response = TestGetAccountPhasedTransactions.phasedTransactionsApiCall().invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");

        for(String idString:transactionIds){
            Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, idString));
        }
    }
}