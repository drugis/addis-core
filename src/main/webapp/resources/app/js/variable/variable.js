'use strict';
define([
  './addVariableController',
  './editVariableController',
  './resultPropertiesDirective',
  'angular',
  'angular-resource'
], function(
  AddVariableController,
  EditVariableController,
  resultProperties,
  angular
) {
    return angular.module('trialverse.variable', ['ngResource', 'trialverse.util'])
      .controller('AddVariableController', AddVariableController)
      .controller('EditVariableController', EditVariableController)

      .directive('resultProperties', resultProperties);
  }
);
