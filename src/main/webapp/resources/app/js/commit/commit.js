'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.commit', [])

    .controller('CommitController', require('commit/commitController'))

    .directive('commitDialog', require('commit/commitDialogDirective'))

    ;
});
