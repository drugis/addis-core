'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.studyInformation', ['ngResource', 'trialverse.util', 'trialverse.study'])
    // controllers
    .controller('EditStudyInformationController', require('studyInformation/editStudyInformationController'))

    //services
    .factory('StudyInformationService', require('studyInformation/studyInformationService'))
    ;
});
