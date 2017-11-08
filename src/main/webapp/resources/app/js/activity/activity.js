'use strict';
var requires = [
  'activity/activityController',
  'activity/activityService',
  'drug/drugService',
  'activity/treatmentDirective'
];
define(requires.concat(['angular', 'angular-resource']), function(
  ActivityController,
  ActivityService,
  DrugService,
  treatment,
  angular
) {
  var dependencies = ['ngResource',
    'trialverse.study',
    'trialverse.util'
  ];
  return angular.module('trialverse.activity', dependencies)
    // controllers
    .controller('ActivityController', ActivityController)

    //services
    .factory('ActivityService', ActivityService)
    .factory('DrugService', DrugService)

    //directives
    .directive('treatment', treatment);
});