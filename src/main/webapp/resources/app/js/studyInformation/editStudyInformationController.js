'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback'
    ];
    var EditStudyInformationController = function($scope, $state, $modalInstance,
      itemService, callback) {

      $scope.blindingOptions = [{
        uri: 'http://trials.drugis.org/ontology#OpenLabel',
        label: 'Open'
      }, {
        uri: 'http://trials.drugis.org/ontology#SingleBlind',
        label: 'Single blind'
      }, {
        uri: 'http://trials.drugis.org/ontology#DoubleBlind',
        label: 'Double blind'
      }, {
        uri: 'http://trials.drugis.org/ontology#TripleBlind',
        label: 'Triple blind'
      }, {
        uri: 'http://trials.drugis.org/ontology#UnknownBlind',
        label: 'Unknown'
      }];

      var itemScratch = angular.copy($scope.item);

      $scope.itemScratch = itemScratch;

      $scope.editItem = function() {
        itemService.editItem($scope.itemScratch).then(function() {
            $scope.item = angular.copy($scope.itemScratch);
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
    return dependencies.concat(EditStudyInformationController);
  });