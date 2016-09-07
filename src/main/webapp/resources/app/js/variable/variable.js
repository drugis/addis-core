'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.variable', ['ngResource', 'trialverse.util'])
    .controller('AddVariableController', require('variable/addVariableController'))
    .controller('EditVariableController', require('variable/editVariableController'))

    .directive('resultProperties', require('variable/resultPropertiesDirective'))
     ;
});
