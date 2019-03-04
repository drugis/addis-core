'use strict';
define([
  './addEpochController',
  './editEpochController',
  './epochService',
  'angular',
  'angular-resource'
],
  function(AddEpochController,
    EditEpochController,
    EpochService,
    angular
  ) {
    return angular.module('trialverse.epoch', ['ngResource', 'trialverse.util', 'trialverse.study'])
      // controllers
      .controller('AddEpochController', AddEpochController)
      .controller('EditEpochController', EditEpochController)

      //services
      .factory('EpochService', EpochService);
  }
);
