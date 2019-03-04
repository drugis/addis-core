'use strict';
define([
  './resultPropertiesService',
  './addVariableController',
  './editVariableController',
  './resultPropertiesDirective',
  'angular',
  'angular-resource'
], function(
  ResultPropertiesService,
  AddVariableController,
  EditVariableController,
  resultProperties,
  angular
) {
    return angular.module('trialverse.variable', ['ngResource', 'trialverse.util'])
      .factory('ResultPropertiesService', ResultPropertiesService)

      .controller('AddVariableController', AddVariableController)
      .controller('EditVariableController', EditVariableController)

      .directive('resultProperties', resultProperties)
      ;
  }
);
