'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'doNavigate', 'stayHere'];
    var UnsavedChangesWarningModalController = function($scope, $modalInstance, doNavigate, stayHere) {

      $scope.yes = function() {
        doNavigate();
        $modalInstance.close();
      };

      $scope.cancel = function() {
        stayHere();
        $modalInstance.dismiss('cancel');
      };

    };

    return dependencies.concat(UnsavedChangesWarningModalController);

  });
