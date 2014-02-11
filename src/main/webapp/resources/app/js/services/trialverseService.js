'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var TrialverseService = function ($resource) {
    return $resource('/trialverse');
  };
  return dependencies.concat(TrialverseService);
});
