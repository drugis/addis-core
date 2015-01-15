'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.measurementMoment', ['ngResource', 'trialverse.study'])
    // controllers
    .controller('MeasurementMomentController', require('measurementMoment/measurementMomentController'))

    //services
    .factory('MeasurementMomentService', require('measurementMoment/measurementMomentService'))
    ;
});
