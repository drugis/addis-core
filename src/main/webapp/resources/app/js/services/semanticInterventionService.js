'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var SemanticInterventionService = function ($resource) {
    return $resource('/namespaces/:id/interventions', {id: '@id'});
  };
  return dependencies.concat(SemanticInterventionService);
});
