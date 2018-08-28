'use strict';
define([
  './adverseEventService',
  'angular',
  'angular-resource'],
  function(AdverseEventService, angular) {
    return angular.module('trialverse.adverseEvent', ['ngResource', 'trialverse.util', 'trialverse.outcome'])
      // //services
      .factory('AdverseEventService', AdverseEventService);
  }
);
