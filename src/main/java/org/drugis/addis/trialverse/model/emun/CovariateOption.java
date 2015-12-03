package org.drugis.addis.trialverse.model.emun;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.drugis.addis.trialverse.model.emun.serializer.CovariateOptionDeserializer;
import org.drugis.addis.trialverse.model.emun.serializer.CovariateOptionSerializer;
import org.drugis.addis.trialverse.service.TriplestoreService;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by connor on 12/1/15.
 */
@JsonDeserialize(using = CovariateOptionDeserializer.class)
@JsonSerialize(using = CovariateOptionSerializer.class)
public enum CovariateOption {

  ALLOCATION_RANDOMIZED(CovariateOptionType.STUDY_CHARACTERISTIC, "Allocation: Randomized", TriplestoreService.loadResource("sparql/covariateNaQuery.sparql")),
  BLINDING_AT_LEAST_SINGLE_BLIND(CovariateOptionType.STUDY_CHARACTERISTIC, "Blinding: at least Single Blind", TriplestoreService.loadResource("sparql/covariateNaQuery.sparql")),
  BLINDING_AT_LEAST_DOUBLE_BLIND(CovariateOptionType.STUDY_CHARACTERISTIC, "Blinding: at least Double Blind", TriplestoreService.loadResource("sparql/covariateZeroQuery.sparql")),
  MULTI_CENTER_STUDY(CovariateOptionType.STUDY_CHARACTERISTIC, "Multi-center study", TriplestoreService.loadResource("sparql/covariateZeroQuery.sparql"));

  private CovariateOptionType type;
  private String label;
  private String query;

  CovariateOption(CovariateOptionType type, String label, String query) {
    this.type = type;
    this.label = label;
    this.query = query;
  }

  public static CovariateOption fromKey(String key) {
    Optional<CovariateOption> optionOptional = Arrays.stream(CovariateOption.values())
            .filter(option -> option.toString().equals(key)).findFirst();
    if (optionOptional.isPresent()) {
      return optionOptional.get();
    } else {
      throw new EnumConstantNotPresentException(CovariateOption.class, key);
    }
  }

  public CovariateOptionType getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }

  public String getQuery() {
    return query;
  }
}
