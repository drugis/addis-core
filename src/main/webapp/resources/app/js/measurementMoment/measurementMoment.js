'use strict';
var requires = [
  'measurementMoment/measurementMomentController',
  'measurementMoment/measurementMomentService',
  'util/filters/anchorFilter'
];
define(requires.concat(['angular', 'angular-resource']), function(
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
});