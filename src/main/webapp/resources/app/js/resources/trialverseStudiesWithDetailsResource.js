'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var TrialverseStudiesWithDetailsResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studiesWithDetail', {
      namespaceUid: '@id'
    }, {
      'get': {
        isArray: true
      }
    });
  };
  return dependencies.concat(TrialverseStudiesWithDetailsResource);
});