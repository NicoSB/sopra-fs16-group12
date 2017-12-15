package ch.uzh.ifi.seal.soprafs16.model.action;

import ch.uzh.ifi.seal.soprafs16.model.GameDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.CollectItemRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.DrawOrPlayCardRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveMarshalRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.MoveRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.PunchRequestDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.actionRequest.ShootRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("actionRequestDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CollectItemRequestDTO.class, name = "collectItemRequestDTO"),
        @JsonSubTypes.Type(value = MoveMarshalRequestDTO.class, name = "moveMarshalRequestDTO"),
        @JsonSubTypes.Type(value = MoveRequestDTO.class, name = "moveRequestDTO"),
        @JsonSubTypes.Type(value = PunchRequestDTO.class, name = "punchRequestDTO"),
        @JsonSubTypes.Type(value = ShootRequestDTO.class, name = "shootRequestDTO"),
        @JsonSubTypes.Type(value = DrawOrPlayCardRequestDTO.class, name = "drawOrPlayCardRequestDTO")
})
public class ActionRequestDTO extends ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JsonIgnore
    private GameDTO game;

    public GameDTO getGame() {
        return game;
    }

    public void setGame(GameDTO game) {
        this.game = game;
    }

}
