'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'StudyResource', '$location', '$anchorScroll', '$modal', 'StudyService'];
    var StudyController = function($scope, $stateParams, StudyResource, $location, $anchorScroll, $modal, StudyService) {

      $scope.study = {};
      $scope.arms = {};
      $scope.categorySettings =  {
        populationCharacteristics: {
          service: 'PopulationCharacteristicService',
          anchorId: 'populationCharacteristics',
          header: 'Population Characteristics',
          addItemController: 'CreatePopulationCharacteristicController',
          categoryEmptyMessage: 'No population characteristics in study.',
          itemName: 'Population Characteristic',
          itemTemplateUrl: 'app/js/populationCharacteristic/populationCharacteristic.html',
          addItemTemplateUrl: 'app/js/populationCharacteristic/addPopulationCharacteristic.html',
          editItemTemplateUrl: 'app/js/populationCharacteristic/editPopulationCharacteristic.html',
          editItemController: 'EditPopulationCharacteristicController',
        }
      }

      $scope.isStudyModified = function() {
        return StudyService.isStudyModified();
      };

      StudyResource.get($stateParams, function(response) {
        StudyService.
        loadStore(response.n3Data)
          .then(function(numberOfTriples) {
            console.log('loading study-store success, ' + numberOfTriples + ' triples loaded');

            StudyService.queryStudyData().then(function(studyQueryResult) {
              $scope.study = studyQueryResult;
            });
            reloadArms();
          }, function() {
            console.error('failed loading study-store');
          });
      });

      $scope.sideNavClick = function(anchor) {
        var newHash = anchor;
        if ($location.hash() !== newHash) {
          $location.hash(anchor);
        } else {
          $anchorScroll();
        }
      };

      function reloadArms() {
        StudyService.queryArmData().then(function(armsQueryResult) {
          $scope.arms = armsQueryResult;
        });
      }
      $scope.reloadArms = reloadArms;

      $scope.showArmDialog = function() {
        $modal.open({
          templateUrl: 'app/js/study/view/arm.html',
          scope: $scope,
          controller: 'CreateArmController',
          resolve: {
            successCallback: function() {
              return reloadArms;
            }
          }
        });
      };

      $scope.saveStudy = function() {
        StudyService.exportGraph().then(function(graph) {
          StudyResource.put({
            datasetUUID: $stateParams.datasetUUID,
            studyUUID: $stateParams.studyUUID
          }, graph, function() {
            console.log('graph saved');
            StudyService.studySaved();
          });
        });
      };
    };
    return dependencies.concat(StudyController);
  });
