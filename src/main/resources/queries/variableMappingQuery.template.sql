SELECT studies.id AS study_id, concepts.id AS concept_id
FROM
  variables,
  concepts,
  concept_map,
  studies
WHERE variables.study_id = studies.id
AND concepts.id = variables.variable_concept
AND concept_map.sub = concepts.id
AND concept_map.super = CAST(:variable AS uuid)
AND studies.id IN (:studies)