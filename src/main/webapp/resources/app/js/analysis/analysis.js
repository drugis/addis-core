'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource'];

  return angular.module('addis.analysis',
    dependencies)
    // controllers
    .controller('AddAnalysisController', require('analysis/addAnalysisController'))

    //services

    //filter

    ;
});
