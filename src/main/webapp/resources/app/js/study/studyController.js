'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$window', 'VersionedGraphResource', '$location', '$anchorScroll',
      '$modal', 'StudyService', 'ResultsService', 'StudyDesignService'
    ];
    var StudyController = function($scope, $stateParams, $window, VersionedGraphResource, $location, $anchorScroll,
      $modal, StudyService, ResultsService, StudyDesignService) {

      // onload
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
        $(navbar[0]).css('margin-top', this.pageYOffset - 20);
        $scope.$apply();
      });

      function reloadStudyModel() {
        VersionedGraphResource.get({
          datasetUUID: $stateParams.datasetUUID,
          graphUuid: $stateParams.studyUUID,
          versionUuid: $stateParams.versionUuid
        }, function(response) {
          StudyService.loadStore(response.data)
            .then(function() {
              console.log('loading study-store success');
              StudyService.queryStudyData().then(function(queryResult) {
                $scope.study = queryResult;
                $scope.$broadcast('refreshStudyDesign');
                $scope.$broadcast('refreshResults');
                StudyService.studySaved();
              });
            }, function() {
              console.error('failed loading study-store');
            });
        });
      }

      $scope.resetStudy = function() {
        reloadStudyModel();
      };

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
              return function(newVersion) {
                StudyService.studySaved();
                $location.path('/datasets/' + $stateParams.datasetUUID + '/versions/' + newVersion + '/studies/' + $stateParams.studyUUID);
              };
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