'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.study', ['ngResource',
    'trialverse.util',
    'trialverse.arm',
    'trialverse.populationCharacteristic'])
    // controllers
    .controller('StudyController', require('study/studyController'))
    .controller('CreateArmController', require('study/createArmController'))

    //services
    .factory('StudyService', require('study/studyService'))

    //resources
    .factory('StudyResource', require('study/studyResource'))

    //directives
    .directive('studyCategory', require('study/categoryDirective'))
    .directive('categoryItemDirective', require('study/categoryItemDirective'))
    ;
});
