package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.request.CollectItemRequestDTO;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

@Entity
@JsonTypeName("collectCard")
public class CollectCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public ActionRequestDTO createActionRequest(GameDTO game, User user) {
        CollectItemRequestDTO request = new CollectItemRequestDTO();
        request.setGame(game);

        request.setHasBag(Boolean.FALSE);
        request.setHasCase(Boolean.FALSE);
        request.setHasGem(Boolean.FALSE);
        if (!user.getWagonLevel().getItems().isEmpty()) {
            for (int i = 0; i < user.getWagonLevel().getItems().size(); i++) {
                if (user.getWagonLevel().getItems().get(i).getItemType() == ItemType.GEM) {
                    request.setHasGem(Boolean.TRUE);
                }
                if (user.getWagonLevel().getItems().get(i).getItemType() == ItemType.BAG) {
                    request.setHasBag(Boolean.TRUE);
                }
                if (user.getWagonLevel().getItems().get(i).getItemType() == ItemType.CASE) {
                    request.setHasCase(Boolean.TRUE);
                }
            }
        }

        if (!request.getHasBag() && !request.getHasCase() && !request.getHasGem()) {
            return null;
        }
        request.setSpielId(game.getId());
        request.setUserId(user.getId());

        return request;
    }
}
