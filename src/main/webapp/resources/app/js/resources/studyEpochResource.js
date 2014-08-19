'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var studyEpochResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid/epochs/:epochUid', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id',
      epochUid: '@epochUid'
    });
  };
  return dependencies.concat(studyEpochResource);
});