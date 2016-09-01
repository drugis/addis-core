'use strict';

define(function(require) {
  var angular = require('angular');

  return angular.module('trialverse.study', ['ngResource',
      'trialverse.util',
      'trialverse.arm',
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
    .controller('EditVariableController', require('study/editVariableController'))
    .controller('UnsavedChangesWarningModalController', require('study/unsavedChanges/unsavedChangesWarningModalController'))

  //services
  .factory('StudyService', require('study/studyService'))
  .factory('StudyReadOnlyService', require('study/studyReadOnlyService'))

  //resources
  .factory('CopyStudyResource', require('study/copyStudyResource'))
  .factory('DatasetResource', require('dataset/datasetResource'))

  //directives
  .directive('studyCategory', require('study/categoryDirective'))
  .directive('categoryItemDirective', require('study/categoryItemDirective'))
  .directive('variableCategory', require('study/variableCategoryDirective'))
    ;
});
