'use strict';

define(function(require) {
  var angular = require('angular');

  return angular.module('trialverse.study', ['ngResource',
      'trialverse.util',
      'trialverse.arm',
      'trialverse.variable',
      'trialverse.populationCharacteristic',
      'trialverse.endpoint',
      'trialverse.adverseEvent',
      'trialverse.measurementMoment',
      'trialverse.results',
      'trialverse.graph',
      'trialverse.activity'
    ])
    // controllers
    .controller('StudyController', require('study/studyController'))
    .controller('CopyStudyController', require('study/copyStudyController'))
    .controller('EditStudyController', require('study/editStudyController'))
    .controller('UnsavedChangesWarningModalController', require('study/unsavedChanges/unsavedChangesWarningModalController'))
    .controller('D80TableController', require('study/d80TableController'))

  //services
  .factory('StudyService', require('study/studyService'))
  .factory('D80TableService', require('study/d80TableService'))

  //resources
  .factory('CopyStudyResource', require('study/copyStudyResource'))
  .factory('DatasetResource', require('dataset/datasetResource'))
.factory('EstimatesResource',require('study/estimatesResource'))
  //directives
  .directive('studyCategory', require('study/categoryDirective'))
  .directive('categoryItemDirective', require('study/categoryItemDirective'))
  .directive('variableCategory', require('study/variableCategoryDirective'))
    ;
});
