'use strict';
define(['./editArmController',
  './createArmController',
  './armService',
  'angular', 
  'angular-resource',
],
  function(
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
  }
);
