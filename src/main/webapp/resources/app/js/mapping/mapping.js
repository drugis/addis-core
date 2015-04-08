'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.mapping', [])

    // controllers
    .controller('MappingController', require('mapping/mappingController'))

    //services
    .factory('MappingService', require('mapping/mappingService'))

  ;

});
