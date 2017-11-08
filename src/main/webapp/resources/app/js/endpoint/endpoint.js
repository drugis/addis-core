'use strict';
var requires = ['endpoint/endpointService'];
define(requires.concat(['angular', 'angular-resource']), function(
  EndpointService,
  angular
) {
  return angular.module('trialverse.endpoint', ['ngResource', 'trialverse.util', 'trialverse.outcome'])
    //services
    .factory('EndpointService', EndpointService);
});