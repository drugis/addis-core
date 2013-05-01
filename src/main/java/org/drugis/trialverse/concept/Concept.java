package org.drugis.trialverse.concept;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.drugis.common.hibernate.PostgresEnumConverter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@TypeDefs({
	@TypeDef(name="conceptType", typeClass=PostgresEnumConverter.class,
			 parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.concept.ConceptType")})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE,
		isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@NoArgsConstructor
@Data public class Concept implements Identifiable<UUID>  {
	@Id @JsonIgnore @Type(type="pg-uuid") @GeneratedValue(strategy=GenerationType.IDENTITY) private UUID id;
	@Basic(fetch=FetchType.LAZY) private String name;
	@Basic(fetch=FetchType.LAZY) private String description;
	@Basic(fetch=FetchType.LAZY) @Type(type="conceptType") private ConceptType type;
	@Basic(fetch=FetchType.LAZY) private String code;
	@ManyToOne(fetch=FetchType.LAZY) private CodeSystem codeSystem;
	@Basic(fetch=FetchType.LAZY) private String owner;

	public Concept(final UUID id) {
		this.id = id;
	}
}