'use strict';
define(function(require) {
  var angular = require('angular', '');
  return angular.module('addis.resources', ['ngResource'])
    .factory('ProjectsService', require('services/projectsService'))
    .factory('TrialverseService', require('services/trialverseService'));
});
