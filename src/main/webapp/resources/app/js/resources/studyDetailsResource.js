'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyDetailsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUuid', {
      namespaceUid: '@namespaceUid',
      studyUuid: '@id'
    });
  };
  return dependencies.concat(StudyDetailsResource);
});