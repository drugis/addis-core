SELECT DISTINCT studies.*
FROM studies,
  study_variables,
  concepts AS study_concepts,
  concept_map,
  concepts AS variable_concepts
  JOIN (
      (SELECT CAST(id AS uuid) FROM (VALUES 
        (:variables)
      ) AS _(id))
    )
    AS request_variables (id)
    ON variable_concepts.id = request_variables.id,
  treatments,
  concepts AS treatment_concepts
  JOIN (
      (SELECT CAST(id AS uuid) FROM (VALUES 
        (:treatments)
      ) AS _(id))
    )
    AS request_treatments (id)
    ON treatment_concepts.id = request_treatments.id
WHERE studies.indication_concept = CAST(:indication AS uuid)
AND studies.id = study_variables.study_id 
AND study_concepts.id = study_variables.variable_concept
AND concept_map.sub = study_concepts.id
AND concept_map.super = variable_concepts.id
AND variable_concepts.id = request_variables.id
AND studies.id = treatments.study_id
AND treatment_concepts.id = treatments.drug_concept