'use strict';
var requires = [
  'covariates/addCovariateController',
  'covariates/covariateResource',
  'covariates/covariateOptionsResource'
];
define(requires.concat(['angular', 'angular-resource']), function(
  AddCovariateController,
  CovariateResource,
  CovariateOptionsResource,
  angular
) {
  var dependencies = ['ngResource'];
  return angular.module('addis.covariates', dependencies)
    // controllers
    .controller('AddCovariateController', AddCovariateController)

    // resources
    .factory('CovariateResource', CovariateResource)
    .factory('CovariateOptionsResource', CovariateOptionsResource);
});