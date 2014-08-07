'use strict';
define([], function () {
  var dependencies = ['$resource'];
  var TrialverseTrialDataResource = function ($resource) {
    return $resource('/namespaces/:namespaceUid/trialData', {
      namespaceUid: '@id'
    });
  };
  return dependencies.concat(TrialverseTrialDataResource);
});