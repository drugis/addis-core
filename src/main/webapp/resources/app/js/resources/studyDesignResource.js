'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyDesignResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUuid/studyDesign', {
      namespaceUid: '@namespaceUid',
      studyUuid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(StudyDesignResource);
});