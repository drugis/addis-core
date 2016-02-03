'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.endpoint', ['ngResource', 'trialverse.util', 'trialverse.outcome'])
    // controllers
     .controller('AddEndpointController', require('endpoint/addEndpointController'))
     .controller('EditEndpointController', require('endpoint/editEndpointController'))

    // //services
     .factory('EndpointService', require('endpoint/endpointService'))
     ;
});
