'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var studyTreatmentActivityResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUuid/treatmentActivities/:treatmentActivityUid', {
      namespaceUid: '@namespaceUid',
      studyUuid: '@id',
      treatmentActivityUid: '@armUid'
    });
  };
  return dependencies.concat(studyTreatmentActivityResource);
});