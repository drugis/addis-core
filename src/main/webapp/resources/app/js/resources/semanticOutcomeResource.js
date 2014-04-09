'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var SemanticOutcomeResource = function ($resource) {
    return $resource('/namespaces/:id/outcomes', {id: '@id'});
  };
  return dependencies.concat(SemanticOutcomeResource);
});
