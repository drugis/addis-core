'use strict';
define([], function () {
  var dependencies = ['$resource'];
  var EvidenceTableResource = function ($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/evidenceTable');
  };
  return dependencies.concat(EvidenceTableResource);
});