'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$window', '$filter',
      'VersionedGraphResource', 'GraphResource', '$location', '$anchorScroll',
      '$modal', 'StudyService', 'ResultsService', 'StudyDesignService', 'DatasetResource'
    ];
    var StudyController = function($scope, $stateParams, $window, $filter,
      VersionedGraphResource, GraphResource, $location, $anchorScroll,
      $modal, StudyService, ResultsService, StudyDesignService, DatasetResource) {

      $scope.userUid = $stateParams.userUid;
      $scope.datasetUUID = $stateParams.datasetUUID;
      if($stateParams.versionUuid) {
        $scope.versionUuid = $stateParams.versionUuid;
      }
      $scope.studyGraphUuid = $stateParams.studyGraphUuid;
      $scope.openCopyDialog = openCopyDialog;
      $scope.study = {};
      $scope.resetStudy = resetStudy;

      // onload
      StudyService.reset();



      $scope.categorySettings = {
        studyInformation: {
          service: 'StudyInformationService',
          anchorId: 'study-information',
          header: 'Study Information',
          itemName: 'Study Information',
          itemTemplateUrl: 'app/js/studyInformation/studyInformation.html',
          editItemTemplateUrl: 'app/js/studyInformation/editStudyInformation.html',
          editItemController: 'EditStudyInformationController'
        },
        populationInformation: {
          service: 'PopulationInformationService',
          anchorId: 'population-information',
          header: 'Population Information',
          itemName: 'Population Information',
          itemTemplateUrl: 'app/js/populationInformation/populationInformation.html',
          editItemTemplateUrl: 'app/js/populationInformation/editPopulationInformation.html',
          editItemController: 'EditPopulationInformationController',
        },
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

      $scope.conceptSettings = {
        drugs: {
          label: 'Drugs',
          serviceName: 'DrugService',
          typeUri: 'ontology:Drug'
        },
        populationCharacteristics: {
          label: 'Population characteristics',
          serviceName: 'PopulationCharacteristicService',
          typeUri: 'ontology:Variable'
        },
        endpoints: {
          label: 'Endpoints',
          serviceName: 'EndpointService',
          typeUri: 'ontology:Variable'
        },
        adverseEvents: {
          label: 'Adverse events',
          serviceName: 'AdverseEventService',
          typeUri: 'ontology:Variable'
        }
      };

      function openCopyDialog() {
        $modal.open({
          templateUrl: 'app/js/study/copyStudy.html',
          controller: 'CopyStudyController',
          resolve: {
            datasets: function() {
              return DatasetResource.queryForJson({
                userUid: $scope.loginUser.id
              }).$promise.then(function(result) {
                return _.filter(result, function(dataset) {
                  return dataset.uri !== 'http://trials.drugis.org/datasets/' + $scope.datasetUUID;
                });
              });
            },
            userUuid: function() {
              return $scope.loginUser.id;
            },
            datasetUuid: function() {
              return $stateParams.datasetUUID;
            },
            graphUuid: function() {
              return $stateParams.studyGraphUuid;
            },
            versionUuid: function() {
              return $scope.versionUuid;
            },
            successCallback: function() {
              return $scope.reloadDatasets;
            }
          }
        });
      }

      function reloadStudyModel() {
        // load the data from the backend
        var getStudyFromBackendDefer;
        if ($stateParams.versionUuid) {
          getStudyFromBackendDefer = VersionedGraphResource.getJson({
            userUid: $stateParams.userUid,
            datasetUUID: $stateParams.datasetUUID,
            graphUuid: $stateParams.studyGraphUuid,
            versionUuid: $stateParams.versionUuid
          });
        } else {
          getStudyFromBackendDefer = GraphResource.getJson({
            userUid: $stateParams.userUid,
            datasetUUID: $stateParams.datasetUUID,
            graphUuid: $stateParams.studyGraphUuid
          });
        }

        // place loaded data into fontend cache
        StudyService.loadJson(getStudyFromBackendDefer.$promise);

        // use the loaded data to fill the view and alert the subviews
        getStudyFromBackendDefer.$promise.then(function() {
          StudyService.getStudy().then(function(study) {
            $scope.studyUuid = $filter('stripFrontFilter')(study['@id'], 'http://trials.drugis.org/studies/');
            $scope.study = {
              id: $scope.studyUuid,
              label: study.label,
              comment: study.comment,

            };
            $scope.$broadcast('refreshStudyDesign');
            $scope.$broadcast('refreshResults');
            StudyService.studySaved();
          });
        });

      }

      function resetStudy() {
        // skip reset check in controller as ng-disabled does not work with a <a> tag needed by foundation menu item
        if (StudyService.isStudyModified()) {
          reloadStudyModel();
        }
      }

      reloadStudyModel();

      $scope.$on('updateStudyDesign', function() {
        ResultsService.cleanupMeasurements().then(function() {
          $scope.$broadcast('refreshResults');
        });
        StudyDesignService.cleanupCoordinates($stateParams.studyUUID).then(function() {
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
        // skip save check in controller as ng-disabled does not work with a <a> tag needed by foundation menu item
        if (!StudyService.isStudyModified()) {
          return;
        }

        $modal.open({
          templateUrl: 'app/js/commit/commit.html',
          controller: 'CommitController',
          resolve: {
            callback: function() {
              return function(newVersion) {
                $scope.loadStudiesWithDetail();
                StudyService.studySaved();
                $location.path('/users/' + $stateParams.userUid + '/datasets/' + $stateParams.datasetUUID + '/versions/' + newVersion + '/studies/' + $stateParams.studyGraphUuid);
              };
            },
            userUid: function() {
              return $stateParams.userUid;
            },
            datasetUuid: function() {
              return $stateParams.datasetUUID;
            },
            graphUuid: function() {
              return $stateParams.studyGraphUuid;
            },
            itemServiceName: function() {
              return 'StudyService';
            }
          }
        });
      };

      var navbar = document.getElementsByClassName('side-nav');
      angular.element($window).bind('scroll', function() {
        $(navbar[0]).css('margin-top', this.pageYOffset - 20);
        $scope.$apply();
      });

      $scope.navSettings = {
        isCompact: false,
        isHidden: false
      };

      function calculateNavSettings() {
        var windowHeight = $window.innerHeight;
        $scope.navSettings.isCompact = windowHeight < 1199;
        $scope.navSettings.isHidden = windowHeight < 799;
      }

      // check if the menu still fits on resize
      angular.element($window).bind('resize', function() {
        $scope.$apply(calculateNavSettings());
      });
      // initial setup
      calculateNavSettings();

    };
    return dependencies.concat(StudyController);
  });
