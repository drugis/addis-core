'use strict';
define([], function () {
  var dependencies = ['$resource'];
  var TrialverseTrialDataResource = function ($resource) {
    return $resource('/namespaces/:id/trialData', {
      id: '@id'
    });
  };
  return dependencies.concat(TrialverseTrialDataResource);
});