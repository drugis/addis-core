'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var studyEpochResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUuid/epochs/:epochUid', {
      namespaceUid: '@namespaceUid',
      studyUuid: '@id',
      epochUid: '@epochUid'
    });
  };
  return dependencies.concat(studyEpochResource);
});