'use strict';
var requires = [
  'adverseEvent/adverseEventService'
];
define(requires.concat(['angular', 'angular-resource']), function(AdverseEventService) {
  var angular = require('angular');
  return angular.module('trialverse.adverseEvent', ['ngResource', 'trialverse.util', 'trialverse.outcome'])
    // //services
    .factory('AdverseEventService', AdverseEventService);
});