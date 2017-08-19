
package egl.http;

import egl.Account;
import egl.Attachment;
import egl.EagleException;
import egl.Shuffling;
import egl.ShufflingParticipant;
import egl.util.Convert;
import egl.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static egl.http.JSONResponses.INCORRECT_PUBLIC_KEY;

public final class ShufflingProcess extends CreateTransaction {

    static final ShufflingProcess instance = new ShufflingProcess();

    private ShufflingProcess() {
        super(new APITag[]{APITag.SHUFFLING, APITag.CREATE_TRANSACTION},
                "shuffling", "recipientSecretPhrase", "recipientPublicKey");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws EagleException {
        Shuffling shuffling = ParameterParser.getShuffling(req);
        if (shuffling.getStage() != Shuffling.Stage.PROCESSING) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 11);
            response.put("errorDescription", "Shuffling is not in processing, stage " + shuffling.getStage());
            return JSON.prepare(response);
        }
        Account senderAccount = ParameterParser.getSenderAccount(req);
        long senderId = senderAccount.getId();
        if (shuffling.getAssigneeAccountId() != senderId) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 12);
            response.put("errorDescription", String.format("Account %s cannot process shuffling since shuffling assignee is %s",
                    Convert.rsAccount(senderId), Convert.rsAccount(shuffling.getAssigneeAccountId())));
            return JSON.prepare(response);
        }
        ShufflingParticipant participant = shuffling.getParticipant(senderId);
        if (participant == null) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 13);
            response.put("errorDescription", String.format("Account %s is not a participant of shuffling %d",
                    Convert.rsAccount(senderId), shuffling.getId()));
            return JSON.prepare(response);
        }

        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        byte[] recipientPublicKey = ParameterParser.getPublicKey(req, "recipient");
        if (Account.getAccount(recipientPublicKey) != null) {
            return INCORRECT_PUBLIC_KEY; // do not allow existing account to be used as recipient
        }

        Attachment.ShufflingAttachment attachment = shuffling.process(senderId, secretPhrase, recipientPublicKey);
        return createTransaction(req, senderAccount, attachment);
    }

}
