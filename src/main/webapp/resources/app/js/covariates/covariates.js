'use strict';
define([
  './addCovariateController',
  './covariateResource',
  './covariateOptionsResource',
  'angular',
  'angular-resource'
],
  function(
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
  }
);
