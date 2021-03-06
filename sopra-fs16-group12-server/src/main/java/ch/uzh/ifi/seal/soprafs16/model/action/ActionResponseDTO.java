package ch.uzh.ifi.seal.soprafs16.model.action;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.CollectItemResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.DrawCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.MoveMarshalResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.MoveResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.PlayCardResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.PunchResponseDTO;
import ch.uzh.ifi.seal.soprafs16.model.action.response.dtos.ShootResponseDTO;


/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("actionResponseDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CollectItemResponseDTO.class, name = "collectItemResponseDTO"),
        @JsonSubTypes.Type(value = MoveMarshalResponseDTO.class, name = "moveMarshalResponseDTO"),
        @JsonSubTypes.Type(value = MoveResponseDTO.class, name = "moveResponseDTO"),
        @JsonSubTypes.Type(value = PunchResponseDTO.class, name = "punchResponseDTO"),
        @JsonSubTypes.Type(value = ShootResponseDTO.class, name = "shootResponseDTO"),
        @JsonSubTypes.Type(value = DrawCardResponseDTO.class, name = "drawCardResponseDTO"),
        @JsonSubTypes.Type(value = PlayCardResponseDTO.class, name = "playCardResponseDTO")
})
public abstract class ActionResponseDTO extends ActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
}
