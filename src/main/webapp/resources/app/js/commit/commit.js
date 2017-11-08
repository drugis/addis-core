'use strict';
var requires = [
  'commit/commitController',
  'commit/commitDialogDirective'
];
define(requires.concat(['angular']), function(
  CommitController,
  commitDialog,
  angular
) {
  return angular.module('trialverse.commit', [])
    .controller('CommitController', CommitController)
    .directive('commitDialog', commitDialog)
    ;
});