'use strict';
var requires = [
  'variable/addVariableController',
  'variable/editVariableController',
  'variable/resultPropertiesDirective'
];
define(requires.concat(['angular', 'angular-resource']), function(
  AddVariableController,
  EditVariableController,
  resultProperties,
  angular) {
  return angular.module('trialverse.variable', ['ngResource', 'trialverse.util'])
    .controller('AddVariableController', AddVariableController)
    .controller('EditVariableController', EditVariableController)

    .directive('resultProperties', resultProperties);
});