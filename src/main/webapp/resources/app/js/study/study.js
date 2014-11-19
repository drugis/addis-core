'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.study', [])
    .controller('StudyController', require('study/studyController'))
    ;
});