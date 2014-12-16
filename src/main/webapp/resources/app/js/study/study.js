'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.study', ['ngResource',
    'trialverse.util',
    'trialverse.arm',
    'trialverse.populationCharacteristic',
    'trialverse.endpoint'])
    // controllers
    .controller('StudyController', require('study/studyController'))

    //services
    .factory('StudyService', require('study/studyService'))

    //resources
    .factory('StudyResource', require('study/studyResource'))

    //directives
    .directive('studyCategory', require('study/categoryDirective'))
    .directive('categoryItemDirective', require('study/categoryItemDirective'))
    ;
});
