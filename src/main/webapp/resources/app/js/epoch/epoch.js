'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.epoch', ['ngResource', 'trialverse.util', 'trialverse.study'])
    // controllers
    .controller('AddEpochController', require('epoch/addEpochController'))
    .controller('EditEpochController', require('epoch/editEpochController'))

    //services
    .factory('EpochService', require('epoch/epochService'))
    ;
});
