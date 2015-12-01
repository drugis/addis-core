'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var CovariateOptionsResource = function ($resource) {
    return $resource('/covariate-options');
  };
  return dependencies.concat(CovariateOptionsResource);
});
