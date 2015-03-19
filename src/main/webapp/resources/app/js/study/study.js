'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.study', ['ngResource',
    'trialverse.util',
    'trialverse.arm',
    'trialverse.populationCharacteristic',
    'trialverse.endpoint',
    'trialverse.adverseEvent',
    'trialverse.measurementMoment',
    'trialverse.results',
    'trialverse.graph'])
    // controllers
    .controller('StudyController', require('study/studyController'))

    //services
    .factory('StudyService', require('study/studyService'))

    //resources
    .factory('DatasetResource', require('dataset/datasetResource'))

    //directives
    .directive('studyCategory', require('study/categoryDirective'))
    .directive('categoryItemDirective', require('study/categoryItemDirective'))
    ;
});
