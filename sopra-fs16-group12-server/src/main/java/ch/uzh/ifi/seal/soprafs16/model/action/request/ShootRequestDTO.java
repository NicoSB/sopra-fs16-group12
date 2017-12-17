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
@JsonTypeName("shootRequestDTO")
public class ShootRequestDTO extends ActionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ElementCollection
    private List<Long> shootableUserIds;

    public ShootRequestDTO()
    {
        this.shootableUserIds = new ArrayList<>();
    }
    public List<Long> getShootableUserIds() {
        return shootableUserIds;
    }

    public void setShootableUserIds(List<Long> shootableUserIds) {
        this.shootableUserIds = shootableUserIds;
    }
}
