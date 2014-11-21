'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.study', [])
    // controllers
    .controller('StudyController', require('study/studyController'))

    //services
    .factory('StudyService', require('study/studyService'))

    //resources
    .factory('StudyResource', require('study/studyResource'))
    ;
});
