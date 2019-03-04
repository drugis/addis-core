'use strict';
define([
  './studyDesignService',
  '../arm/armService',
  '../epoch/epochService',
  '../activity/activityService',
  './studyDesignDirective',
  'angular',
  'angular-resource'
],
  function(
    StudyDesignService,
    ArmService,
    EpochService,
    ActivityService,
    studyDesign,
    angular
  ) {
    var dependencies = ['ngResource',
      'trialverse.study',
      'trialverse.util'
    ];
    return angular.module('trialverse.studyDesign', dependencies)
      // controllers

      //services
      .factory('StudyDesignService', StudyDesignService)
      .factory('ArmService', ArmService)
      .factory('EpochService', EpochService)
      .factory('ActivityService', ActivityService)

      //directives
      .directive('studyDesign', studyDesign);
  }
);
