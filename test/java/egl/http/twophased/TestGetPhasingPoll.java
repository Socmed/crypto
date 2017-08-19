
package egl.http.twophased;


import egl.BlockchainTest;
import egl.Constants;
import egl.http.APICall;
import egl.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestGetPhasingPoll extends BlockchainTest {

    @Test
    public void transactionVotes() {

        APICall apiCall = new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder()
                .quorum(1)
                .build();
        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        String fullHash = (String) transactionJSON.get("fullHash");
        String transactionId = (String) transactionJSON.get("transaction");

        generateBlock();

        long fee = Constants.ONE_EGL;
        apiCall = new APICall.Builder("approveTransaction")
                .param("secretPhrase", CHUCK.getSecretPhrase())
                .param("transactionFullHash", fullHash)
                .param("feeNQT", fee)
                .build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("approveTransactionResponse:" + response.toJSONString());

        generateBlock();

        apiCall = new APICall.Builder("getPhasingPoll")
                .param("transaction", transactionId)
                .param("countVotes", "true")
                .build();
        response = apiCall.invoke();
        Logger.logMessage("getPhasingPollResponse:" + response.toJSONString());

        Assert.assertNull(response.get("errorCode"));
        Assert.assertEquals(1, Integer.parseInt((String) response.get("result")));
    }

}