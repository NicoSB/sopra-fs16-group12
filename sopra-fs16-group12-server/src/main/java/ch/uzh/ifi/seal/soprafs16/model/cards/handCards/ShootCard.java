package ch.uzh.ifi.seal.soprafs16.model.cards.handCards;

import ch.uzh.ifi.seal.soprafs16.constant.LevelType;
import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import ch.uzh.ifi.seal.soprafs16.model.WagonLevel;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.ShootRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.characters.Belle;
import ch.uzh.ifi.seal.soprafs16.model.characters.Django;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.User;

@Entity
@JsonTypeName("shootCard")
public class ShootCard extends ActionCard implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private User user;

    @Override
    public ActionRequestDTO createActionRequest(GameDTO game, User user) {

        ShootRequestDTO srq = new ShootRequestDTO();
        List<User> userList = new ArrayList<>();
        srq.setShootableUserIds(new ArrayList<>());
        if (user.getWagonLevel().getLevelType() == LevelType.TOP) {
            getShootableUsersBeforeRoof(user, userList, user.getWagonLevel());
            getShootableUsersAfterRoof(user, userList, user.getWagonLevel());
        }
        if (user.getWagonLevel().getLevelType() == LevelType.BOTTOM) {
            getShootableUsersBeforeBottom(user, userList, user.getWagonLevel());
            getShootableUsersAfterBottom(user, userList, user.getWagonLevel());
        }
        if (userList.size() >= 2) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getCharacter() instanceof Belle) {
                    userList.remove(i);
                }
            }
        }

        for (User anUserList : userList) {
            srq.getShootableUserIds().add(anUserList.getId());
        }

        if (srq.getShootableUserIds().isEmpty()) {
            return null;
        }
        srq.setSpielId(game.getId());
        srq.setUserId(user.getId());

        return srq;
    }
    public void getShootableUsersBeforeRoof(User user, List<User> shootable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelBefore() != null) {
            int size = wagonLevel.getWagonLevelBefore().getUsers().size();
            for (int i = 0; i < size; i++) {
                shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
            }
            if (shootable.isEmpty()) {
                getShootableUsersBeforeRoof(user, shootable, wagonLevel.getWagonLevelBefore());
            }
            if (user.getCharacter() instanceof Django) {
                getShootableUsersBeforeRoof(user, shootable, wagonLevel.getWagonLevelBefore());
            }
        }
    }

    public void getShootableUsersAfterRoof(User user, List<User> shootable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelAfter() != null) {
            int size = wagonLevel.getWagonLevelAfter().getUsers().size();
            for (int i = 0; i < size; i++) {
                shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
            }
            if (shootable.isEmpty()) {
                getShootableUsersAfterRoof(user, shootable, wagonLevel.getWagonLevelAfter());
            }
            if (user.getCharacter() instanceof Django) {
                getShootableUsersAfterRoof(user, shootable, wagonLevel.getWagonLevelAfter());
            }
        }
    }

    public void getShootableUsersBeforeBottom(User user, List<User> shootable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelBefore() != null) {
            int size = wagonLevel.getWagonLevelBefore().getUsers().size();
            for (int i = 0; i < size; i++) {
                shootable.add(wagonLevel.getWagonLevelBefore().getUsers().get(i));
            }
            if (user.getCharacter() instanceof Django && shootable.isEmpty()) {
                getShootableUsersBeforeBottom(user, shootable, wagonLevel.getWagonLevelBefore());
            }
        }
    }

    public void getShootableUsersAfterBottom(User user, List<User> shootable, WagonLevel wagonLevel) {
        if (wagonLevel.getWagonLevelAfter() != null) {
            int size = wagonLevel.getWagonLevelAfter().getUsers().size();
            for (int i = 0; i < size; i++) {
                shootable.add(wagonLevel.getWagonLevelAfter().getUsers().get(i));
            }
            if (shootable.isEmpty() && user.getCharacter() instanceof Django) {
                getShootableUsersAfterBottom(user, shootable, wagonLevel.getWagonLevelAfter());
            }
        }
    }

}
