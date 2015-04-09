'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modal', 'MappingService'];

    var MappingController = function($scope, $stateParams, $modal, MappingService) {

      function reloadMappings() {
        $scope.mappings = MappingService.queryItems($stateParams.datasetUUID);
      }

      $scope.openMappingDialog = function() {
        $modal.open({
          templateUrl: 'app/js/concept/createMapping.html',
          controller: 'CreateMappingController',
          resolve: {
            callback: function() {
              return reloadMappings;
            }
          }
        });
      }

    };
    return dependencies.concat(MappingController);
  });