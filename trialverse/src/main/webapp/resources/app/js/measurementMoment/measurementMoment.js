'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource',
  'trialverse.study',
  'trialverse.epoch',
  'trialverse.util'];

  return angular.module('trialverse.measurementMoment',
    dependencies)
    // controllers
    .controller('MeasurementMomentController', require('measurementMoment/measurementMomentController'))

    //services
    .factory('MeasurementMomentService', require('measurementMoment/measurementMomentService'))

    //filter
    .filter('anchorFilter', require('util/filters/anchorFilter'))
    ;
});
