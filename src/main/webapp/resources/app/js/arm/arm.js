'use strict';
var requires = [
  'arm/editArmController',
  'arm/createArmController',
  'arm/armService'
];
define(requires.concat(['angular', 'angular-resource']), function(
  EditArmController,
  CreateArmController,
  ArmService,
  angular
) {
  return angular.module('trialverse.arm', ['ngResource', 'trialverse.util', 'trialverse.study'])
    // controllers
    .controller('EditArmController', EditArmController)
    .controller('CreateArmController', CreateArmController)

    //services
    .factory('ArmService', ArmService);
});