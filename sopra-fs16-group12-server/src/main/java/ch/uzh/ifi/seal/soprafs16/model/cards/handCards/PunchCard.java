package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.User;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.request.PunchRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.characters.Belle;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Entity;

@Entity
@JsonTypeName("punchCard")
public class PunchCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public ActionRequestDTO createActionRequest(GameDTO game, User user) {
        PunchRequestDTO request = new PunchRequestDTO();
        List<User> userList = new ArrayList<>();
        request.setPunchableUserIds(new ArrayList<>());

        for (User u : user.getWagonLevel().getUsers()) {
            if (!u.getId().equals(user.getId())) {
                userList.add(u);
            }
        }
        if (userList.size() > 1) {
            for (int i = 0; i < userList.size(); i++) {
                if (Objects.equals(userList.get(i).getId(), user.getId())) {
                    userList.remove(i);
                }
            }
        }
        if (userList.size() >= 2) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getCharacter() instanceof Belle) {
                    userList.remove(i);
                }
            }
        }
        for (int i = 0; i < userList.size(); i++) {
            request.getHasBag().add(i, Boolean.FALSE);
            request.getHasCase().add(i, Boolean.FALSE);
            request.getHasGem().add(i, Boolean.FALSE);
            request.getPunchableUserIds().add(userList.get(i).getId());

            for (int j = 0; j < userList.get(i).getItems().size(); j++) {
                switch (userList.get(i).getItems().get(j).getItemType()) {
                    case GEM:
                        request.getHasGem().set(i, Boolean.TRUE);
                        break;
                    case BAG:
                        request.getHasBag().set(i, Boolean.TRUE);
                        break;
                    case CASE:
                        request.getHasCase().set(i, Boolean.TRUE);
                        break;

                }
            }
        }
        if (user.getWagonLevel().getWagonLevelBefore() != null) {
            request.getMovable().add(user.getWagonLevel().getWagonLevelBefore().getId());
        }
        if (user.getWagonLevel().getWagonLevelAfter() != null) {
            request.getMovable().add(user.getWagonLevel().getWagonLevelAfter().getId());
        }

        if (request.getPunchableUserIds().isEmpty()) {
            return null;
        }

        request.setSpielId(game.getId());
        request.setUserId(user.getId());

        return request;
    }
}
