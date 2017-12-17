package ch.uzh.ifi.seal.soprafs16.model.action.request;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("collectItemRequestDTO")
public class CollectItemRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column
    private Boolean hasCase;
    @Column
    private Boolean hasGem;
    @Column
    private Boolean hasBag;

    public Boolean getHasCase() {
        return hasCase;
    }

    public void setHasCase(Boolean hasCase) {
        this.hasCase = hasCase;
    }

    public Boolean getHasGem() {
        return hasGem;
    }

    public void setHasGem(Boolean hasGem) {
        this.hasGem = hasGem;
    }

    public Boolean getHasBag() {
        return hasBag;
    }

    public void setHasBag(Boolean hasBag) {
        this.hasBag = hasBag;
    }
}
