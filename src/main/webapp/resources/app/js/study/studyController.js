'use strict';
define(['angular', 'lodash'],
  function(angular, _) {
    var dependencies = [
      '$scope',
      '$q',
      '$stateParams',
      '$filter',
      '$location',
      '$modal',
      '$anchorScroll',
      '$transitions',
      '$window',
      'DatasetResource',
      'ExcelExportService',
      'GraphResource',
      'PageTitleService',
      'ResultsService',
      'StudyDesignService',
      'StudyService',
      'UserService',
      'VersionedGraphResource',
      'STUDY_CATEGORY_SETTINGS'
    ];
    var StudyController = function(
      $scope,
      $q,
      $stateParams,
      $filter,
      $location,
      $modal,
      $anchorScroll,
      $transitions,
      $window,
      DatasetResource,
      ExcelExportService,
      GraphResource,
      PageTitleService,
      ResultsService,
      StudyDesignService,
      StudyService,
      UserService,
      VersionedGraphResource,
      STUDY_CATEGORY_SETTINGS
    ) {
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
      $scope.study = {};
      StudyService.reset();
      $scope.categorySettings = STUDY_CATEGORY_SETTINGS;

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
          templateUrl: './unsavedChanges/unsavedWarningModal.html',
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
        angular.element(navbar[0]).css('margin-top', (this.pageYOffset - 20) + 'px');
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
          templateUrl: './editStudy.html',
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
          templateUrl: './view/d80Table.html',
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
          templateUrl: './copyStudy.html',
          controller: 'CopyStudyController',
          resolve: {
            datasets: function() {
              return DatasetResource.queryForJson({
                userUid: $scope.loggedInUser.id
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
          PageTitleService.setPageTitle('StudyController', $scope.study.label);
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
          templateUrl: '../commit/commit.html',
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
