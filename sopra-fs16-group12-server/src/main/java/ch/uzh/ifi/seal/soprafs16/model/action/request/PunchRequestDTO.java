package ch.uzh.ifi.seal.soprafs16.model.action.request;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionRequestDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("punchRequestDTO")
public class PunchRequestDTO extends ActionRequestDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    @ElementCollection
    private List<Long> punchableUserIds;
    @ElementCollection
    private List<Boolean> hasGem;
    @ElementCollection
    private List<Boolean> hasBag;
    @ElementCollection
    private List<Boolean> hasCase;
    @ElementCollection
    private List<Long> movable;

    public PunchRequestDTO()
    {
        this.punchableUserIds = new ArrayList<>();
        this.hasGem = new ArrayList<>();
        this.hasBag = new ArrayList<>();
        this.hasCase = new ArrayList<>();
        this.movable = new ArrayList<>();
    }

    public List<Long> getPunchableUserIds() {
        return punchableUserIds;
    }

    public void setPunchableUserIds(List<Long> punchableUserIds) {
        this.punchableUserIds = punchableUserIds;
    }

    public List<Boolean> getHasGem() {
        return hasGem;
    }

    public void setHasGem(List<Boolean> hasGem) {
        this.hasGem = hasGem;
    }

    public List<Boolean> getHasBag() {
        return hasBag;
    }

    public void setHasBag(List<Boolean> hasBag) {
        this.hasBag = hasBag;
    }

    public List<Boolean> getHasCase() {
        return hasCase;
    }

    public void setHasCase(List<Boolean> hasCase) {
        this.hasCase = hasCase;
    }

    public List<Long> getMovable() {
        return movable;
    }

    public void setMovable(List<Long> movable) {
        this.movable = movable;
    }
}
