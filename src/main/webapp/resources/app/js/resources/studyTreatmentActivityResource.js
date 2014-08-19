'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var studyTreatmentActivityResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid/treatmentActivities/:treatmentActivityUid', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id',
      treatmentActivityUid: '@armUid'
    });
  };
  return dependencies.concat(studyTreatmentActivityResource);
});