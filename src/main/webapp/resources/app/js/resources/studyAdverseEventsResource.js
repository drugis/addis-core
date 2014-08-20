'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyAdverseEventsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid/studyData/adverseEvents', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(StudyAdverseEventsResource);
});