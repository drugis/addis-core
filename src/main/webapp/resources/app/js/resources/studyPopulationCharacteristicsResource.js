'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyPopulationCharacteristicsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUuid/studyData/populationCharacteristics', {
      namespaceUid: '@namespaceUid',
      studyUuid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(StudyPopulationCharacteristicsResource);
});