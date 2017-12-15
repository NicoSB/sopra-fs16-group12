package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveMarshalRequestDTO;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

@Entity
@JsonTypeName("marshalCard")
public class MarshalCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public ActionRequestDTO createActionRequest(GameDTO game, User user) {
        MoveMarshalRequestDTO request = new MoveMarshalRequestDTO();

        if (game.getMarshal().getWagonLevel().getWagonLevelBefore() != null) {
            request.getMovableWagonsLvlIds().add(game.getMarshal().getWagonLevel().getWagonLevelBefore().getId());
        }
        if (game.getMarshal().getWagonLevel().getWagonLevelAfter() != null) {
            request.getMovableWagonsLvlIds().add(game.getMarshal().getWagonLevel().getWagonLevelAfter().getId());
        }

        request.setSpielId(game.getId());
        request.setUserId(user.getId());

        return request;
    }
}
