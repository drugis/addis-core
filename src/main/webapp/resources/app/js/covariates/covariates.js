'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource'];

  return angular.module('addis.covariates', dependencies)
    // controllers
    .controller('AddCovariateController', require('covariates/addCovariateController'))

    // resources
    .factory('CovariateResource', require('covariates/covariateResource'))

    //services


    //directives

    ;
});
