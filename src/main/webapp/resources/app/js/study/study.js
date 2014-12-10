'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.study', ['ngResource', 'trialverse.util', 'trialverse.arm'])
    // controllers
    .controller('StudyController', require('study/studyController'))
    .controller('CreateArmController', require('study/createArmController'))

    //services
    .factory('StudyService', require('study/studyService'))

    //resources
    .factory('StudyResource', require('study/studyResource'))
    ;
});
