'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback', 'item'
    ];
    var EditStudyInformationController = function($scope, $state, $modalInstance,
      itemService, callback, item) {

      $scope.item = item;

      $scope.editItem = function() {
        itemService.editItem($scope.item).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      };

      $scope.isValidNumberOfCenters = function(a) {
        return a === undefined || Number.isInteger(a);
      }

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditStudyInformationController);
  });
