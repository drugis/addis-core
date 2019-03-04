'use strict';
define([
  './measurementMomentController',
  './measurementMomentService',
  '../util/filters/anchorFilter',
  'angular',
  'angular-resource',
  '../epoch/epoch'
],
  function(
    MeasurementMomentController,
    MeasurementMomentService,
    anchorFilter,
    angular) {
    var dependencies = ['ngResource',
      'trialverse.study',
      'trialverse.epoch',
      'trialverse.util'
    ];

    return angular.module('trialverse.measurementMoment',
      dependencies)
      // controllers
      .controller('MeasurementMomentController', MeasurementMomentController)

      //services
      .factory('MeasurementMomentService', MeasurementMomentService)

      //filter
      .filter('anchorFilter', anchorFilter);
  }
);
