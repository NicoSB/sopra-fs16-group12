package ch.uzh.ifi.seal.soprafs16.model.action.response.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("punchResponseDTO")
public class PunchResponseDTO extends ActionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long victimId;
    private Long wagonLevelId;
    private ItemType itemType;

    public ItemType getItemType() {
        return itemType;
    }

    public Long getWagonLevelId() {
        return wagonLevelId;
    }

    public Long getVictimId() {
        return victimId;
    }

    public void setVictimId(Long victimId) {
        this.victimId = victimId;
    }

    public void setWagonLevelId(Long wagonLevelId) {
        this.wagonLevelId = wagonLevelId;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}
