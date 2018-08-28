'use strict';
define([
  './studyController',
  './copyStudyController',
  './editStudyController',
  './unsavedChanges/unsavedChangesWarningModalController',
  './d80TableController',
  './studyService',
  './d80TableService',
  './copyStudyResource',
  '../dataset/datasetResource',
  './estimatesResource',
  './categoryDirective',
  './categoryItemDirective',
  './variableCategoryDirective',
  'angular',
  'angular-resource'
],
  function(
    StudyController,
    CopyStudyController,
    EditStudyController,
    UnsavedChangesWarningModalController,
    D80TableController,
    StudyService,
    D80TableService,
    CopyStudyResource,
    DatasetResource,
    EstimatesResource,
    studyCategory,
    categoryItemDirective,
    variableCategory,
    angular
  ) {
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
      .controller('StudyController', StudyController)
      .controller('CopyStudyController', CopyStudyController)
      .controller('EditStudyController', EditStudyController)
      .controller('UnsavedChangesWarningModalController', UnsavedChangesWarningModalController)
      .controller('D80TableController', D80TableController)

      //services
      .factory('StudyService', StudyService)
      .factory('D80TableService', D80TableService)

      //resources
      .factory('CopyStudyResource', CopyStudyResource)
      .factory('DatasetResource', DatasetResource)
      .factory('EstimatesResource', EstimatesResource)
      //directives
      .directive('studyCategory', studyCategory)
      .directive('categoryItemDirective', categoryItemDirective)
      .directive('variableCategory', variableCategory);
  }
);
