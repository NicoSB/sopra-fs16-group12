package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.request.MoveRequestDTO;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;


@Entity
@JsonTypeName("moveCard")
public class MoveCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public ActionRequestDTO createActionRequest(GameDTO game, User user) {
        MoveRequestDTO request = new MoveRequestDTO();
        List<Long> movable = new ArrayList<>();
        request.setMovableWagonsLvlIds(new ArrayList<>());

        if (user.getWagonLevel().getLevelType() == LevelType.TOP) {
            getMovableBeforeRoof(movable, user.getWagonLevel());
            getMovableAfterRoof(movable, user.getWagonLevel());

            if (movable.size() > 3) {
                for (int i = 0; i < 3; i++) {
                    request.getMovableWagonsLvlIds().add(movable.get(i));
                }
            } else {
                for (Long aMovable : movable) {
                    request.getMovableWagonsLvlIds().add(aMovable);
                }
            }
        }

        if (user.getWagonLevel().getLevelType() == LevelType.BOTTOM) {
            if (user.getWagonLevel().getWagonLevelBefore() != null) {
                request.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelBefore().getId());
            }
            if (user.getWagonLevel().getWagonLevelAfter() != null) {
                request.getMovableWagonsLvlIds().add(user.getWagonLevel().getWagonLevelAfter().getId());
            }
        }

        if (request.getMovableWagonsLvlIds().isEmpty()) {
            return null;
        }

        request.setSpielId(game.getId());
        request.setUserId(user.getId());

        return request;
    }

    public void getMovableBeforeRoof(List<Long> movable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelBefore() != null) {
            movable.add(wagonLevel.getWagonLevelBefore().getId());
            getMovableBeforeRoof(movable, wagonLevel.getWagonLevelBefore());
        }
    }

    public void getMovableAfterRoof(List<Long> movable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelAfter() != null) {
            movable.add(wagonLevel.getWagonLevelAfter().getId());
            getMovableAfterRoof(movable, wagonLevel.getWagonLevelAfter());
        }
    }
}
