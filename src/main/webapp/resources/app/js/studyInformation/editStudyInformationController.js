'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback', 'item'
    ];
    var EditStudyInformationController = function($scope, $state, $modalInstance,
      itemService, callback, item) {
      // functions
      $scope.editItem = editItem;
      $scope.isValidNumberOfCenters = isValidNumberOfCenters;
      $scope.cancel = cancel;

      // init
      $scope.item = item;

      function editItem() {
        itemService.editItem($scope.item).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function isValidNumberOfCenters(a) {
        return a === undefined || Number.isInteger(a);
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditStudyInformationController);
  });