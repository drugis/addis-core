'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.epoch', ['ngResource', 'trialverse.util', 'trialverse.study'])
    // controllers
    .controller('EditEpochController', require('epoch/editEpochController'))
    .controller('AddEpochController', require('epoch/addEpochController'))

    //services
    .factory('EpochService', require('epoch/epochService'))
    ;
});
