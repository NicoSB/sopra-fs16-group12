package ch.uzh.ifi.seal.soprafs16.model.action.response;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.actions.Action;

public class ActionResponse {
    private Action action;
    private ActionResponseDTO response;

    public void executeAction() {
        action.execute(response);
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ActionResponseDTO getResponse() {
        return response;
    }

    public void setResponse(ActionResponseDTO response) {
        this.response = response;
    }
}
