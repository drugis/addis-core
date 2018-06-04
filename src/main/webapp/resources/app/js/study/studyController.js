'use strict';
define(['angular', 'lodash'],
  function(angular, _) {
    var dependencies = ['$scope', '$q', '$state', '$stateParams', '$window', '$filter', '$transitions',
      'VersionedGraphResource', 'GraphResource', '$location', '$anchorScroll',
      '$modal', 'StudyService', 'ResultsService', 'StudyDesignService', 'DatasetResource',
      'ExcelExportService',
      'UserService'
    ];
    var StudyController = function($scope, $q, $state, $stateParams, $window, $filter, $transitions,
      VersionedGraphResource, GraphResource, $location, $anchorScroll,
      $modal, StudyService, ResultsService, StudyDesignService, DatasetResource,
      ExcelExportService,
      UserService) {
      // functions
      $scope.sideNavClick = sideNavClick;
      $scope.saveStudy = saveStudy;
      $scope.openCopyDialog = openCopyDialog;
      $scope.resetStudy = resetStudy;
      $scope.showEditStudyModal = showEditStudyModal;
      $scope.showD80Table = showD80Table;
      $scope.isStudyModified = isStudyModified;
      $scope.exportStudy = exportStudy;

      // init
      $scope.datasetUuid = $stateParams.datasetUuid;
      $scope.userUid = $stateParams.userUid;
      if ($stateParams.versionUuid) {
        $scope.versionUuid = $stateParams.versionUuid;
      }
      $scope.studyGraphUuid = $stateParams.studyGraphUuid;
      $scope.hasLoggedInUser = UserService.hasLoggedInUser();
      $scope.study = {};
      StudyService.reset();
      $scope.categorySettings = {
        studyInformation: {
          service: 'StudyInformationService',
          anchorId: 'study-information',
          helpId: 'study-information',
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
          helpId: 'population-information',
          itemName: 'Population Information',
          itemTemplateUrl: 'app/js/populationInformation/populationInformation.html',
          editItemTemplateUrl: 'app/js/populationInformation/editPopulationInformation.html',
          editItemController: 'EditPopulationInformationController',
        },
        arms: {
          service: 'ArmService',
          anchorId: 'arms',
          helpId: 'arm',
          header: 'Arms',
          addItemController: 'CreateArmController',
          categoryEmptyMessage: 'No arms defined.',
          itemName: 'arm',
          itemTemplateUrl: 'app/js/arm/arm.html',
          addItemTemplateUrl: 'app/js/arm/addArm.html',
          editItemTemplateUrl: 'app/js/arm/editArm.html',
          editItemController: 'EditArmController',
          repairItemTemplateUrl: 'app/js/arm/repairArm.html',
          repairItemController: 'EditArmController'
        },
        groups: {
          service: 'GroupService',
          anchorId: 'groups',
          helpId: 'other-group',
          header: 'Other groups',
          addItemController: 'CreateGroupController',
          categoryEmptyMessage: 'No other groups defined.',
          itemName: 'group',
          itemTemplateUrl: 'app/js/arm/arm.html',
          addItemTemplateUrl: 'app/js/group/addGroup.html',
          editItemTemplateUrl: 'app/js/group/editGroup.html',
          editItemController: 'EditGroupController',
          repairItemTemplateUrl: 'app/js/group/repairGroup.html',
          repairItemController: 'EditGroupController'
        },
        baselineCharacteristics: {
          service: 'PopulationCharacteristicService',
          anchorId: 'baselineCharacteristics',
          helpId: 'baseline-characteristic',
          header: 'Baseline characteristics',
          addItemTemplateUrl: 'app/js/variable/addVariable.html',
          addItemController: 'AddVariableController',
          categoryEmptyMessage: 'No baseline characteristics defined.',
          itemName: 'baseline characteristic',
          itemTemplateUrl: 'app/js/variable/variable.html',
          editItemTemplateUrl: 'app/js/variable/editVariable.html',
          editItemController: 'EditVariableController',
          repairItemTemplateUrl: 'app/js/outcome/repairOutcome.html',
          repairItemController: 'EditOutcomeController'
        },
        outcomes: {
          service: 'EndpointService',
          anchorId: 'outcomes',
          helpId: 'trialverse-outcome',
          header: 'Outcomes',
          categoryEmptyMessage: 'No outcomes defined.',
          itemName: 'outcome',
          itemTemplateUrl: 'app/js/variable/variable.html',
          addItemController: 'AddVariableController',
          addItemTemplateUrl: 'app/js/variable/addVariable.html',
          editItemTemplateUrl: 'app/js/variable/editVariable.html',
          editItemController: 'EditVariableController',
          repairItemTemplateUrl: 'app/js/outcome/repairOutcome.html',
          repairItemController: 'EditOutcomeController'
        },
        adverseEvents: {
          service: 'AdverseEventService',
          anchorId: 'adverseEvents',
          helpId: 'adverse-event',
          header: 'Adverse events',
          addItemController: 'AddVariableController',
          addItemTemplateUrl: 'app/js/variable/addVariable.html',
          categoryEmptyMessage: 'No adverse events defined.',
          itemName: 'adverse event',
          itemTemplateUrl: 'app/js/variable/variable.html',
          editItemTemplateUrl: 'app/js/variable/editVariable.html',
          editItemController: 'EditVariableController',
          repairItemTemplateUrl: 'app/js/outcome/repairOutcome.html',
          repairItemController: 'EditOutcomeController'
        },
        epochs: {
          service: 'EpochService',
          anchorId: 'epochs',
          helpId: 'epoch',
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
          helpId: 'measurement-moment',
          header: 'Measurement moments',
          addItemController: 'MeasurementMomentController',
          categoryEmptyMessage: 'No measurement moments defined.',
          itemName: 'measurement moment',
          itemTemplateUrl: 'app/js/measurementMoment/measurementMoment.html',
          addItemTemplateUrl: 'app/js/measurementMoment/editMeasurementMoment.html',
          editItemTemplateUrl: 'app/js/measurementMoment/editMeasurementMoment.html',
          editItemController: 'MeasurementMomentController',
          repairItemTemplateUrl: 'app/js/measurementMoment/repairMeasurementMoment.html',
          repairItemController: 'MeasurementMomentController'
        },
        activities: {
          service: 'ActivityService',
          anchorId: 'activities',
          helpId: 'activity',
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
          helpKey: 'drug',
          serviceName: 'DrugService',
          typeUri: 'ontology:Drug'
        },
        baselineCharacteristics: {
          label: 'Baseline characteristics',
          helpKey: 'baseline-characteristic',
          serviceName: 'PopulationCharacteristicService',
          typeUri: 'ontology:Variable'
        },
        outcomes: {
          label: 'Outcomes',
          helpKey: 'trialverse-outcome',
          serviceName: 'EndpointService',
          typeUri: 'ontology:Variable'
        },
        adverseEvents: {
          label: 'Adverse events',
          helpKey: 'adverse-event',
          serviceName: 'AdverseEventService',
          typeUri: 'ontology:Variable'
        },
        units: {
          label: 'Units',
          helpKey: 'unit',
          serviceName: 'UnitService',
          typeUri: 'ontology:Unit'
        }
      };

      $scope.$on('updateStudyDesign', function() {
        ResultsService.cleanupMeasurements().then(function() {
          $scope.$broadcast('refreshResults');
        });
        StudyDesignService.cleanupCoordinates($stateParams.studyUUID).then(function() {
          $scope.$broadcast('refreshStudyDesign');
        });
      });

      var deRegisterStateChangeStart = $transitions.onStart({
        to: function(state) {
          return state.name !== 'dataset.study' && state.name !== 'versionedDataset.study';
        }
      }, function() {
        if (!StudyService.isStudyModified()) {
          return;
        }
        var stateChangeDeferred = $q.defer();
        $modal.open({
          templateUrl: 'app/js/study/unsavedChanges/unsavedWarningModal.html',
          controller: 'UnsavedChangesWarningModalController',
          windowClass: 'small',
          resolve: {
            doNavigate: function() {
              return function() {
                deRegisterStateChangeStart();
                stateChangeDeferred.resolve(true);
              };
            },
            stayHere: function() {
              return function() {
                stateChangeDeferred.resolve(false);
              };
            }
          }
        });
        return stateChangeDeferred.promise;
      });

      reloadStudyModel();

      var navbar = document.getElementsByClassName('side-nav');
      angular.element($window).bind('scroll', function() {
        $(navbar[0]).css('margin-top', this.pageYOffset - 20);
        $scope.$apply();
      });

      $scope.navSettings = {
        isCompact: false,
        isHidden: false
      };

      // check if the menu still fits on resize
      angular.element($window).bind('resize', function() {
        $scope.$apply(calculateNavSettings());
      });
      // initial setup
      calculateNavSettings();

      function showEditStudyModal() {
        $modal.open({
          templateUrl: 'app/js/study/editStudy.html',
          controller: 'EditStudyController',
          resolve: {
            study: function() {
              return $scope.study;
            },
            successCallback: function() {
              return function(title, description) {
                $scope.study.label = title;
                $scope.study.comment = description;
              };
            }
          }
        });
      }

      function showD80Table() {
        $modal.open({
          templateUrl: 'app/js/study/view/d80Table.html',
          controller: 'D80TableController',
          size: 'large',
          resolve: {
            study: function() {
              return $scope.study;
            }
          }
        });
      }

      function openCopyDialog() {
        $modal.open({
          templateUrl: 'app/js/study/copyStudy.html',
          controller: 'CopyStudyController',
          resolve: {
            datasets: function() {
              return DatasetResource.queryForJson({
                userUid: UserService.getLoginUser().id
              }).$promise.then(function(result) {
                return _.filter(result, function(dataset) {
                  return dataset.uri !== 'http://trials.drugis.org/datasets/' + $scope.datasetUuid;
                });
              });
            },
            userUid: function() {
              return $scope.userUid;
            },
            datasetUuid: function() {
              return $stateParams.datasetUuid;
            },
            graphUuid: function() {
              return $stateParams.studyGraphUuid;
            },
            versionUuid: function() {
              return $scope.currentRevision.uri.split('/versions/')[1];
            },
            successCallback: function() {
              return $scope.reloadDatasets;
            }
          }
        });
      }

      function reloadStudyModel() {
        // load the data from the backend
        var studyPromise;
        if ($stateParams.versionUuid) {
          studyPromise = VersionedGraphResource.getJson({
            userUid: $stateParams.userUid,
            datasetUuid: $stateParams.datasetUuid,
            graphUuid: $stateParams.studyGraphUuid,
            versionUuid: $stateParams.versionUuid
          }).$promise;
        } else {
          studyPromise = GraphResource.getJson({
            userUid: $stateParams.userUid,
            datasetUuid: $stateParams.datasetUuid,
            graphUuid: $stateParams.studyGraphUuid
          }).$promise;
        }

        // place loaded data into frontend cache
        StudyService.loadJson(studyPromise);

        // use the loaded data to fill the view and alert the subviews
        StudyService.getStudy().then(function(study) {
          $scope.studyUuid = $filter('stripFrontFilter')(study['@id'], 'http://trials.drugis.org/studies/');
          $scope.study = {
            id: $scope.studyUuid,
            label: study.label,
            comment: study.comment,
          };
          if (study.has_publication && study.has_publication.length === 1) {
            $scope.study.nctId = study.has_publication[0].registration_id;
            $scope.study.nctUri = study.has_publication[0].uri;
          }
          $scope.$broadcast('refreshStudyDesign');
          $scope.$broadcast('refreshResults');
          StudyService.studySaved();
        });
      }

      function resetStudy() {
        // skip reset check in controller as ng-disabled does not work with a <a> tag needed by foundation menu item
        if (StudyService.isStudyModified()) {
          reloadStudyModel();
        }
      }

      function isStudyModified() {
        return StudyService.isStudyModified();
      }

      function sideNavClick(anchor) {
        var newHash = anchor;
        $anchorScroll.yOffset = 85;
        if ($location.hash() !== newHash) {
          $location.hash(anchor);
        } else {
          $anchorScroll();
        }
      }

      function saveStudy() {
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
                if (deRegisterStateChangeStart) {
                  deRegisterStateChangeStart();
                }
                $location.path('/users/' + $stateParams.userUid + '/datasets/' + $stateParams.datasetUuid + '/versions/' + newVersion + '/studies/' + $stateParams.studyGraphUuid);
              };
            },
            userUid: function() {
              return $stateParams.userUid;
            },
            datasetUuid: function() {
              return $stateParams.datasetUuid;
            },
            graphUuid: function() {
              return $stateParams.studyGraphUuid;
            },
            itemServiceName: function() {
              return 'StudyService';
            }
          }
        });
      }

      function exportStudy() {
        var coordinates = {
          userUid: $stateParams.userUid,
          datasetUuid: $stateParams.datasetUuid,
          versionUuid: $scope.currentRevision.uri.split('/versions/')[1],
          graphUuid: $stateParams.studyGraphUuid
        };
        ExcelExportService.exportStudy(coordinates);
      }

      function calculateNavSettings() {
        var windowHeight = $window.innerHeight;
        $scope.navSettings.isCompact = windowHeight < 1022;
        $scope.navSettings.isHidden = windowHeight < 799;
      }
    };
    return dependencies.concat(StudyController);
  });