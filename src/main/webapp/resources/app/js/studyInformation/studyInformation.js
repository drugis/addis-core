'use strict';
define([
  './editStudyInformationController',
  './studyInformationService',
  'angular',
  'angular-resource'],
  function(
    EditStudyInformationController,
    StudyInformationService,
    angular
  ) {
    return angular.module('trialverse.studyInformation', ['ngResource', 'trialverse.util', 'trialverse.study'])
      // controllers
      .controller('EditStudyInformationController', EditStudyInformationController)

      //services
      .factory('StudyInformationService', StudyInformationService);
  }
);
