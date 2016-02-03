'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource',
  'trialverse.measurementMoment',
  'trialverse.study',
  'trialverse.util'];

  return angular.module('trialverse.outcome',
    dependencies)
    // controllers


    //services
    .factory('OutcomeService', require('outcome/outcomeService'))

    //filter

    ;
});
