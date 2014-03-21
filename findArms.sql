SELECT
 a.* 
FROM
  arms a,
  designs d,
  treatments t,
  measurement_moments mm
WHERE
  a.id = d.arm
AND
  d.activity = t.activity
AND
  mm.name = 'P0D BEFORE_EPOCH_END Main phase'
AND
  mm.epoch = d.epoch
AND
 t.drug = 11;
