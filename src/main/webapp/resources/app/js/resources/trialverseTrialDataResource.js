'use strict';
define([], function () {
  var dependencies = ['$resource'];
  var TrialverseTrialDataResource = function ($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/evidenceTable');
  };
  return dependencies.concat(TrialverseTrialDataResource);
});
