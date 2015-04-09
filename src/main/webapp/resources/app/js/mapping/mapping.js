'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.mapping', [])

    // controllers
    .controller('MappingController', require('mapping/mappingController'))
    .controller('CreateMappingController', require('mapping/createMappingController'))
    
    //services
    .factory('MappingService', require('mapping/mappingService'))

  ;

});
