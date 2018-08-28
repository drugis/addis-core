'use strict';
define([
  './commitController',
  './commitDialogDirective',
  'angular'
],
  function(
    CommitController,
    commitDialog,
    angular
  ) {
    return angular.module('trialverse.commit', [])
      .controller('CommitController', CommitController)
      .directive('commitDialog', commitDialog)
      ;
  }
);
