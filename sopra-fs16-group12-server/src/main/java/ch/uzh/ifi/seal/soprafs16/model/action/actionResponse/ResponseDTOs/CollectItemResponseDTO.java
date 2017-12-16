package ch.uzh.ifi.seal.soprafs16.model.action.actionResponse.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.constant.ItemType;
import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("collectItemResponseDTO")
public class CollectItemResponseDTO extends ActionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private ItemType collectedItemType;

    public ItemType getCollectedItemType() {
        return collectedItemType;
    }

    public void setCollectedItemType(ItemType collectedItemType) {
        this.collectedItemType = collectedItemType;
    }
}
