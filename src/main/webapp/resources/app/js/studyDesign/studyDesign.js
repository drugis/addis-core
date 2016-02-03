'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource',
  'trialverse.study',
  'trialverse.util'];

  return angular.module('trialverse.studyDesign', dependencies)
    // controllers

    //services
    .factory('StudyDesignService', require('studyDesign/studyDesignService'))
    .factory('ArmService', require('arm/armService'))
    .factory('EpochService', require('epoch/epochService'))
    .factory('ActivityService', require('activity/activityService'))

    //directives
    .directive('studyDesign', require('studyDesign/studyDesignDirective'))
    ;
});
