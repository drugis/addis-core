'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback', 'item'
    ];
    var EditPopulationInformationController = function($scope, $state, $modalInstance,
      itemService, callback, item) {

      $scope.itemScratch = item;

      $scope.editItem = function() {
        itemService.editItem($scope.itemScratch).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditPopulationInformationController);
  });
