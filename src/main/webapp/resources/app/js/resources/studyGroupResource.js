'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var studyGroupResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUuid/groups', {
      namespaceUid: '@namespaceUid',
      studyUuid: '@id'
    });
  };
  return dependencies.concat(studyGroupResource);
});
