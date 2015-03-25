'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$window', 'GraphResource', '$location', '$anchorScroll',
      '$modal', 'StudyService', 'DatasetResource', 'DatasetService', 'ResultsService', 'StudyDesignService'
    ];
    var StudyController = function($scope, $stateParams, $window, GraphResource, $location, $anchorScroll,
      $modal, StudyService, DatasetResource, DatasetService, ResultsService, StudyDesignService) {

      StudyService.reset();

      $scope.study = {};
      $scope.categorySettings = {
        arms: {
          service: 'ArmService',
          anchorId: 'arms',
          header: 'Arms',
          addItemController: 'CreateArmController',
          categoryEmptyMessage: 'No arms defined.',
          itemName: 'arm',
          itemTemplateUrl: 'app/js/arm/arm.html',
          addItemTemplateUrl: 'app/js/arm/addArm.html',
          editItemTemplateUrl: 'app/js/arm/editArm.html',
          editItemController: 'EditArmController',
        },
        populationCharacteristics: {
          service: 'PopulationCharacteristicService',
          anchorId: 'populationCharacteristics',
          header: 'Population characteristics',
          addItemController: 'CreatePopulationCharacteristicController',
          categoryEmptyMessage: 'No population characteristics defined.',
          itemName: 'population characteristic',
          itemTemplateUrl: 'app/js/populationCharacteristic/populationCharacteristic.html',
          addItemTemplateUrl: 'app/js/populationCharacteristic/addPopulationCharacteristic.html',
          editItemTemplateUrl: 'app/js/populationCharacteristic/editPopulationCharacteristic.html',
          editItemController: 'EditPopulationCharacteristicController',
        },
        endpoints: {
          service: 'EndpointService',
          anchorId: 'endpoints',
          header: 'Endpoints',
          addItemController: 'AddEndpointController',
          categoryEmptyMessage: 'No endpoints defined.',
          itemName: 'endpoint',
          itemTemplateUrl: 'app/js/endpoint/endpoint.html',
          addItemTemplateUrl: 'app/js/endpoint/addEndpoint.html',
          editItemTemplateUrl: 'app/js/endpoint/editEndpoint.html',
          editItemController: 'EditEndpointController',
        },
        adverseEvents: {
          service: 'AdverseEventService',
          anchorId: 'adverseEvents',
          header: 'Adverse events',
          addItemController: 'AddAdverseEventController',
          categoryEmptyMessage: 'No adverse events defined.',
          itemName: 'adverse event',
          itemTemplateUrl: 'app/js/adverseEvent/adverseEvent.html',
          addItemTemplateUrl: 'app/js/adverseEvent/addAdverseEvent.html',
          editItemTemplateUrl: 'app/js/adverseEvent/editAdverseEvent.html',
          editItemController: 'EditAdverseEventController',
        },
        epochs: {
          service: 'EpochService',
          anchorId: 'epochs',
          header: 'Epochs',
          addItemController: 'AddEpochController',
          categoryEmptyMessage: 'No epochs defined.',
          itemName: 'epoch',
          itemTemplateUrl: 'app/js/epoch/epoch.html',
          addItemTemplateUrl: 'app/js/epoch/addEpoch.html',
          editItemTemplateUrl: 'app/js/epoch/editEpoch.html',
          editItemController: 'EditEpochController',
        },
        measurementMoments: {
          service: 'MeasurementMomentService',
          anchorId: 'measurementMoments',
          header: 'Measurement moments',
          addItemController: 'MeasurementMomentController',
          categoryEmptyMessage: 'No measurement moments defined.',
          itemName: 'measurement moment',
          itemTemplateUrl: 'app/js/measurementMoment/measurementMoment.html',
          addItemTemplateUrl: 'app/js/measurementMoment/editMeasurementMoment.html',
          editItemTemplateUrl: 'app/js/measurementMoment/editMeasurementMoment.html',
          editItemController: 'MeasurementMomentController',
        },
        activities: {
          service: 'ActivityService',
          anchorId: 'activities',
          header: 'Activities',
          addItemController: 'ActivityController',
          categoryEmptyMessage: 'No activities defined.',
          itemName: 'activity',
          itemTemplateUrl: 'app/js/activity/activity.html',
          addItemTemplateUrl: 'app/js/activity/editActivity.html',
          editItemTemplateUrl: 'app/js/activity/editActivity.html',
          editItemController: 'ActivityController',
        }
      };

      var navbar = document.getElementsByClassName('side-nav');
      angular.element($window).bind('scroll', function() {
        $(navbar[0]).css('margin-top', this.pageYOffset);
        $scope.$apply();
      });

      function reloadStudyModel() {
        GraphResource.get({
          datasetUUID: $stateParams.datasetUUID,
          graphUuid: $stateParams.studyUUID
        }, function(response) {
          StudyService.loadStore(response.data)
            .then(function() {
              console.log('loading study-store success');
              StudyService.queryStudyData().then(function(queryResult) {
                $scope.study = queryResult;
                $scope.$broadcast('refreshStudyDesign');
                StudyService.studySaved();
              });
            }, function() {
              console.error('failed loading study-store');
            });
        });
      }

      function reloadDatasetModel() {
        DatasetResource.get($stateParams, function(response) {
          DatasetService.reset();
          DatasetService.loadStore(response.data).then(function() {
            DatasetService.queryDataset().then(function(queryResult) {
              $scope.dataset = queryResult[0];
              $scope.dataset.uuid = $stateParams.datasetUUID;
            });
          });
        });
      }

      $scope.resetStudy = function() {
        reloadStudyModel();
        reloadDatasetModel();
      }

      // onload
      $scope.resetStudy();

      $scope.$on('updateStudyDesign', function() {
        console.log('update design');
        ResultsService.cleanUpMeasurements().then(function() {
          $scope.$broadcast('refreshResults');
        });
        StudyDesignService.cleanupCoordinates($stateParams.studyUUID).then(function() {
          console.log('after cleanup coord');
          $scope.$broadcast('refreshStudyDesign');
        });
      });

      $scope.isStudyModified = function() {
        return StudyService.isStudyModified();
      };

      $scope.sideNavClick = function(anchor) {
        var newHash = anchor;
        $anchorScroll.yOffset = 73;
        if ($location.hash() !== newHash) {
          $location.hash(anchor);
        } else {
          $anchorScroll();
        }
      };

      $scope.saveStudy = function() {
        $modal.open({
          templateUrl: 'app/js/commit/commit.html',
          controller: 'CommitController',
          resolve: {
            callback: function() {
              return StudyService.studySaved;
            },
            datasetUuid: function() {
              return $stateParams.datasetUUID;
            },
            graphUuid: function() {
              return $stateParams.studyUUID;
            },
            itemServiceName: function() {
              return 'StudyService';
            }
          }
        });
      };

    };
    return dependencies.concat(StudyController);
  });
