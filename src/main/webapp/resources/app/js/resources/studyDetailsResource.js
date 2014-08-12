'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var StudyDetailsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id'
    });
  };
  return dependencies.concat(StudyDetailsResource);
});