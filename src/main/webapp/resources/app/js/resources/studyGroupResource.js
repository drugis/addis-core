'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var studyGroupResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid/groups', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id'
    });
  };
  return dependencies.concat(studyGroupResource);
});
