'use strict';
define([], function () {
  var dependencies = ['$resource'];
  var TrialverseService = function ($resource) {
    return $resource('/namespaces/:id', {
      id: '@id'
    });
  };
  return dependencies.concat(TrialverseService);
});