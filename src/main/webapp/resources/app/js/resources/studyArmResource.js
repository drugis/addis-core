'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var studyArmResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail/:studyUid/arms/:armUid', {
      namespaceUid: '@namespaceUid',
      studyUid: '@id',
      armUid: '@armUid'
    });
  };
  return dependencies.concat(studyArmResource);
});