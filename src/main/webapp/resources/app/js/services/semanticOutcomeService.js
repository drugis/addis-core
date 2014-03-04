'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var SemanticOutcomeService = function ($resource) {
    return $resource('/trialverse/:id/outcomes', {id: '@id'});
  };
  return dependencies.concat(SemanticOutcomeService);
});
