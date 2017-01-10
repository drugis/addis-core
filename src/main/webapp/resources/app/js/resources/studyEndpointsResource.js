'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyEndpointsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUuid/studyData/endpoints', {
      namespaceUid: '@namespaceUid',
      studyUuid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(StudyEndpointsResource);
});