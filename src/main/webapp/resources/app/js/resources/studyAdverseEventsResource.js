'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyAdverseEventsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUuid/studyData/adverseEvents', {
      namespaceUid: '@namespaceUid',
      studyUuid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(StudyAdverseEventsResource);
});