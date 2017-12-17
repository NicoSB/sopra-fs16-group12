package ch.uzh.ifi.seal.soprafs16.model.action.response.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

import javax.persistence.Entity;

import ch.uzh.ifi.seal.soprafs16.model.action.ActionResponseDTO;

/**
 * Created by Timon Willi on 17.04.2016.
 */
@Entity
@JsonTypeName("drawCardResponseDTO")
public class DrawCardResponseDTO extends ActionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;
}
