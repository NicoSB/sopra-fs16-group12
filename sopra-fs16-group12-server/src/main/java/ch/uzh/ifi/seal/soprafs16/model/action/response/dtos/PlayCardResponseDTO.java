package ch.uzh.ifi.seal.soprafs16.model.action.response.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("playCardResponseDTO")
public class PlayCardResponseDTO extends ActionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long playedCardId;

    public Long getPlayedCardId() {
        return playedCardId;
    }

    public void setPlayedCardId(Long playedCardId) {
        this.playedCardId = playedCardId;
    }
}
