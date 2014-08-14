'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyDesignResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid/studyDesign', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(StudyDesignResource);
});