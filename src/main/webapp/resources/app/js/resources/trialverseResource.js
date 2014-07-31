'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var TrialverseResource = function($resource) {
    return $resource('/namespaces/:id', {
      id: '@id'
    });
  };
  return dependencies.concat(TrialverseResource);
});