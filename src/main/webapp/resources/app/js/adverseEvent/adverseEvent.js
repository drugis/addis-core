'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.adverseEvent', ['ngResource', 'trialverse.util', 'trialverse.outcome'])
    // controllers
     .controller('AddAdverseEventController', require('adverseEvent/addAdverseEventController'))
     .controller('EditAdverseEventController', require('adverseEvent/editAdverseEventController'))

    // //services
     .factory('AdverseEventService', require('adverseEvent/adverseEventService'))
     ;
});
