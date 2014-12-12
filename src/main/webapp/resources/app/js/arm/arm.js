'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.arm', ['ngResource', 'trialverse.util', 'trialverse.study'])
    // controllers
    .controller('EditArmController', require('arm/editArmController'))
    .controller('CreateArmController', require('arm/createArmController'))

    //services
    .factory('ArmService', require('arm/armService'))
    ;
});
