'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'AdverseEventService', 'successCallback'];
    var addAdverseEventController = function($scope, $modalInstance, AdverseEventService, successCallback) {

      $scope.item = {};

      $scope.addItem = function() {
        AdverseEventService.addItem($scope.item)
          .then(function() {
              successCallback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create adverseEvent');
              $modalInstance.dismiss('cancel');
            });

      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(addAdverseEventController);
  });
