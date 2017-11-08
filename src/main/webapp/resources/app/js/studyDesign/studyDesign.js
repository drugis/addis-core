'use strict';
var requires = [
  'studyDesign/studyDesignService',
  'arm/armService',
  'epoch/epochService',
  'activity/activityService',
  'studyDesign/studyDesignDirective'
];
define(requires.concat(['angular', 'angular-resource']), function(
  StudyDesignService,
  ArmService,
  EpochService,
  ActivityService,
  studyDesign,
  angular) {
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
});