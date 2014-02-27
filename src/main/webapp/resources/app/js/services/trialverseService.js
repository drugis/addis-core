'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var TrialverseService = function ($resource) {
    return $resource('/trialverse/:id', {id: '@id'});
  };
  return dependencies.concat(TrialverseService);
});
