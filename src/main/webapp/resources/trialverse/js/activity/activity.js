'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource',
  'trialverse.study',
  'trialverse.util'];

  return angular.module('trialverse.activity', dependencies)
    // controllers
    .controller('ActivityController', require('activity/activityController'))

    //services
    .factory('ActivityService', require('activity/activityService'))
    .factory('DrugService', require('drug/drugService'))
    .factory('UnitService', require('unit/unitService'))

    //directives
    .directive('treatment', require('activity/treatmentDirective'))
    ;
});
