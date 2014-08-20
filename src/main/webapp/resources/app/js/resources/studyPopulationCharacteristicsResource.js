'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyPopulationCharacteristicsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid/studyData/populationCharacteristics', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(StudyPopulationCharacteristicsResource);
});