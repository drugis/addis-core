'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.study', ['ngResource', 'trialverse.util'])
    // controllers
    .controller('StudyController', require('study/studyController'))
    .controller('CreateArmController', require('study/createArmController'))
    .controller('EditArmController', require('study/directives/arm/editArmController'))

    //services
    .factory('StudyService', require('study/studyService'))
    .factory('ArmService', require('study/armService'))

    //resources
    .factory('StudyResource', require('study/studyResource'))
    
    .directive('studyArm', require('study/directives/arm/armDirective'))
    ;
});
