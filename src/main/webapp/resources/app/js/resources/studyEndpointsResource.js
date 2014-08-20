'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyEndpointsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid/studyData/endpoints', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(StudyEndpointsResource);
});