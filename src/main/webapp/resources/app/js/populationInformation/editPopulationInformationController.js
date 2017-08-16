'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback', 'item'
    ];
    var EditPopulationInformationController = function($scope, $state, $modalInstance,
      itemService, callback, item) {
      // functions
      $scope.editItem = editItem;
      $scope.cancel = cancel;

      // init
      $scope.itemScratch = item;

      function editItem() {
        itemService.editItem($scope.itemScratch).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      }

      function cancel() {
        $modalInstance.dismiss('cancel');
      }
    };
    return dependencies.concat(EditPopulationInformationController);
  });