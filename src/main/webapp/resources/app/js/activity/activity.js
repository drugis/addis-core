'use strict';
define([
  './activityController',
  './activityService',
  '../drug/drugService',
  './treatmentDirective',
  'angular',
  'angular-resource',
  '../study/study',
  '../util/util'],
  function(
    ActivityController,
    ActivityService,
    DrugService,
    treatment,
    angular
  ) {
    var dependencies = ['ngResource',
      'trialverse.study',
      'trialverse.util'
    ];
    return angular.module('trialverse.activity', dependencies)
      // controllers
      .controller('ActivityController', ActivityController)

      //services
      .factory('ActivityService', ActivityService)
      .factory('DrugService', DrugService)

      //directives
      .directive('treatment', treatment);
  }
);
