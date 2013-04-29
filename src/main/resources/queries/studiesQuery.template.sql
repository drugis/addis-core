SELECT DISTINCT studies.*
FROM studies,
  variables,
  concepts AS study_concepts,
  concept_map,
  concepts AS variable_concepts,
  treatments,
  concepts AS treatment_concepts
WHERE studies.indication_concept = CAST(:indication AS uuid)
AND studies.id = variables.study_id 
AND study_concepts.id = variables.variable_concept
AND concept_map.sub = study_concepts.id
AND concept_map.super = variable_concepts.id
AND studies.id = treatments.study_id
AND treatment_concepts.id = treatments.drug_concept
AND CAST(variable_concepts.id AS varchar) IN (:variables)
AND CAST(treatment_concepts.id AS varchar) IN (:treatments)