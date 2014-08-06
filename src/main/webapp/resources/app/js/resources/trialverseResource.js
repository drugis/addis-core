'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var TrialverseResource = function($resource) {
    return $resource('/namespaces/:namespaceUid', {
      namespaceUid: '@id'
    });
  };
  return dependencies.concat(TrialverseResource);
});