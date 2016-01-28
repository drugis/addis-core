'use strict';


// Declare app level module which depends on filters, and services
angular.module('myApp', ['myApp.filters', 'myApp.services', 'myApp.directives', 'myApp.controllers']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/indication', {templateUrl: 'partials/indication.html', controller: 'IndicationController'});
    $routeProvider.when('/drug', {templateUrl: 'partials/indication.html', controller: 'DrugController'});
    $routeProvider.when('/adverseEvent', {templateUrl: 'partials/indication.html', controller: 'AdverseEventController'});
    $routeProvider.when('/endpoint', {templateUrl: 'partials/indication.html', controller: 'EndpointController'});
    $routeProvider.otherwise({redirectTo: '/indication'});
  }])
  .config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
  }]);
