'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.adverseEvent', ['ngResource', 'trialverse.util', 'trialverse.outcome'])
    // //services
     .factory('AdverseEventService', require('adverseEvent/adverseEventService'))
     ;
});
