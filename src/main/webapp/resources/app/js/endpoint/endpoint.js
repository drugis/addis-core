'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.endpoint', ['ngResource', 'trialverse.util', 'trialverse.outcome'])
    // controllers
     .controller('AddEndpointController', require('endpoint/addEndpointController'))

    // //services
     .factory('EndpointService', require('endpoint/endpointService'))
     ;
});
