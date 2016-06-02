UPDATE versionmapping
SET versioneddataseturl = CONCAT('NEWPREFIX', RIGHT(versioneddataseturl, -LENGTH('OLDPREFIX')))
WHERE LEFT(versioneddataseturl, LENGTH('OLDPREFIX')) = 'OLDPREFIX';

UPDATE project
SET datasetversion = CONCAT('NEWPREFIX', RIGHT(datasetversion, -LENGTH('OLDPREFIX')))
WHERE LEFT(datasetversion, LENGTH('OLDPREFIX')) = 'OLDPREFIX';
