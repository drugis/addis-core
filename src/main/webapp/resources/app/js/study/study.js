'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.study', ['ngResource', 'trialverse.util'])
    // controllers
    .controller('StudyController', require('study/studyController'))
    .controller('ArmController', require('study/armController'))

    //services
    .factory('StudyService', require('study/studyService'))

    //resources
    .factory('StudyResource', require('study/studyResource'))
    ;
});
