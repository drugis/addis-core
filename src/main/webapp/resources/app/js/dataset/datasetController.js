'use strict';
define(['lodash'],
  function(_) {
    var dependencies = [
      '$scope',
      '$location',
      '$stateParams',
      '$state',
      '$modal',
      '$filter',
      '$q',
      'ConceptsService',
      'DataModelService',
      'DatasetResource',
      'DatasetService',
      'DatasetVersionedResource',
      'ExcelExportService',
      'GraphResource',
      'HistoryResource',
      'PageTitleService',
      'StudiesWithDetailsService',
      'UserService',
      'VersionedGraphResource',
      'DATASET_TABLE_OPTIONS'
    ];
    var DatasetController = function(
      $scope,
      $location,
      $stateParams,
      $state,
      $modal,
      $filter,
      $q,
      ConceptsService,
      DataModelService,
      DatasetResource,
      DatasetService,
      DatasetVersionedResource,
      ExcelExportService,
      GraphResource,
      HistoryResource,
      PageTitleService,
      StudiesWithDetailsService,
      UserService,
      VersionedGraphResource,
      DATASET_TABLE_OPTIONS
    ) {
      // functions
      $scope.createProjectDialog = createProjectDialog;
      $scope.showEditDatasetModal = showEditDatasetModal;
      $scope.showDeleteStudyDialog = showDeleteStudyDialog;
      $scope.onStudyFilterChange = onStudyFilterChange;
      $scope.toggleFilterOptions = toggleFilterOptions;
      $scope.showTableOptions = showTableOptions;
      $scope.showStudyDialog = showStudyDialog;
      $scope.exportDataset = exportDataset;
      $scope.loadConcepts = loadConcepts; // do not remove, child controller uses it

      // init
      $scope.userUid = $stateParams.userUid;
      $scope.datasetUuid = $stateParams.datasetUuid;
      $scope.isHeadView = !$stateParams.versionUuid;
      if (!$scope.isHeadView) {
        $scope.versionUuid = $stateParams.versionUuid;
      }
      UserService.getLoginUser().then(function(user) {
        $scope.loggedInUser = user;
        $scope.hasLoggedInUser = !!user;
      });
      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.isEditingAllowed = false;
      $scope.isCreateProjectAllowed = false;
      $scope.tableOptions = DATASET_TABLE_OPTIONS;

      loadStudiesWithDetail();
      $scope.datasetConcepts = loadConcepts(); // scope placement for child states
      $scope.datasetConcepts.then(function(concepts) {
        $scope.interventions = filterConcepts(concepts, 'ontology:Drug');
        $scope.variables = filterConcepts(concepts, 'ontology:Variable');
      });

      getDataset();

      function getDataset() {
        var resource = getResource();
        resource.getForJson($stateParams).$promise.then(function(response) {
          var dsResponse = response['@graph'] ? _.reduce(response['@graph'], _.merge) : response;
          $scope.dataset = {
            datasetUuid: $scope.datasetUuid,
            title: getPurlProperty(dsResponse, 'title'),
            comment: getPurlProperty(dsResponse, 'description'),
            creator: getPurlProperty(dsResponse, 'creator')
          };
          PageTitleService.setPageTitle('DatasetController', $scope.dataset.title);
          loadHistory();
        });
      }

      function getResource() {
        if ($scope.isHeadView) {
          return DatasetResource;
        } else {
          return DatasetVersionedResource;
        }
      }

      function filterConcepts(concepts, type) {
        return _(concepts['@graph'])
          .filter(['@type', type])
          .sortBy(['label'])
          .value();
      }

      function loadHistory() {
        var historyCoords = {
          userUid: $stateParams.userUid,
          datasetUuid: $stateParams.datasetUuid
        };
        HistoryResource.query(historyCoords).$promise.then(afterHistoryIsLoaded);
      }

      function afterHistoryIsLoaded(historyItems) {
        if ($scope.isHeadView) {
          $scope.currentRevision = historyItems[historyItems.length - 1];
          $scope.currentRevision.isHead = true;
        } else {
          $scope.currentRevision = findCurrentRevision(historyItems);
          $scope.currentRevision.isHead = isHeadVersion($scope.currentRevision);
          $scope.isHeadView = $scope.currentRevision.isHead;
        }
        checkEditingAllowed();
        $scope.isCreateProjectAllowed = true;
      }

      function isHeadVersion(currentRevision) {
        return currentRevision.historyOrder === 0;
      }

      function findCurrentRevision(items) {
        return _.find(items, function(item) {
          return item.uri.lastIndexOf($stateParams.versionUuid) > 0;
        });
      }

      function isEditAllowedOnVersion() {
        return $scope.currentRevision && $scope.currentRevision.isHead;
      }

      function checkEditingAllowed() {
        UserService.isLoginUserEmail($scope.dataset.creator).then(function(isOwner) {
          $scope.isEditingAllowed = !!($scope.dataset && isOwner && isEditAllowedOnVersion());
        });
      }

      function loadConcepts() {
        var conceptsPromise;
        if ($scope.versionUuid) {
          conceptsPromise = VersionedGraphResource.getConceptJson({
            userUid: $stateParams.userUid,
            datasetUuid: $stateParams.datasetUuid,
            graphUuid: 'concepts',
            versionUuid: $scope.versionUuid
          }).$promise;
        } else {
          conceptsPromise = GraphResource.getConceptJson({
            userUid: $stateParams.userUid,
            datasetUuid: $stateParams.datasetUuid,
            graphUuid: 'concepts',
          }).$promise;
        }
        var cleanedConceptsPromise = conceptsPromise.then(function(conceptsData) {
          return DataModelService.correctUnitConceptType(conceptsData);
        });
        return ConceptsService.loadJson(cleanedConceptsPromise);
      }

      function getPurlProperty(response, propertyName) {
        return response[propertyName] ? response[propertyName] : response['http://purl.org/dc/terms/' + propertyName];
      }

      function exportDataset() {
        $scope.isExporting = true;
        $scope.progress = {
          studiesDone: 0,
          percentage: 0
        };
        $scope.studiesWithDetailPromise
          .then(doExport)
          .then(function() {
            $scope.isExporting = false;
          });
      }

      function doExport() {
        var graphUuids = getGraphUuids();
        var datasetWithCoordinates = getDatasetWithCoordinates();
        return ExcelExportService.exportDataset(datasetWithCoordinates, graphUuids, _.partial(updateExportProgress, graphUuids));
      }

      function getGraphUuids() {
        return _($scope.studiesWithDetail)
          .sortBy('label')
          .map(function(studyWithDetail) {
            return studyWithDetail.graphUri.split('/graphs/')[1];
          })
          .value();
      }

      function getDatasetWithCoordinates() {
        return _.extend({}, $scope.dataset, {
          url: $location.absUrl(),
          userUid: $stateParams.userUid,
          versionUuid: $scope.currentRevision.uri.split('/versions/')[1]
        });
      }

      function updateExportProgress(graphUuids) {
        ++$scope.progress.studiesDone;
        $scope.progress.percentage = $scope.progress.studiesDone * 100 / graphUuids.length;
      }

      function loadStudiesWithDetail() {
        $scope.studiesWithDetailPromise = StudiesWithDetailsService.get($stateParams.userUid, $stateParams.datasetUuid, $stateParams.versionUuid);
        var treatmentActivitiesPromise = StudiesWithDetailsService.getTreatmentActivities($stateParams.userUid, $stateParams.datasetUuid, $stateParams.versionUuid);
        $scope.studiesPromise = $q.all([$scope.studiesWithDetailPromise, treatmentActivitiesPromise]).then(putStudiesOnScope());
      }

      function putStudiesOnScope(result){
          var studiesWithDetail = result[0] instanceof Array ? result[0] : [];
          var treatmentActivities = result[1] instanceof Array ? result[1] : [];
          StudiesWithDetailsService.addActivitiesToStudies(studiesWithDetail, treatmentActivities);
          $scope.studiesWithDetail = studiesWithDetail;
          $scope.filteredStudies = $scope.studiesWithDetail;
      }

      function showTableOptions() {
        $modal.open({
          templateUrl: './tableOptions.html',
          scope: $scope,
          controller: ['$scope', '$modalInstance', function($scope, $modalInstance) {
            $scope.cancel = function() {
              $modalInstance.close();
            };
          }]
        });
      }

      function onStudyFilterChange(filterSelections) {
        $scope.filterSelections = filterSelections; //in case user goes back to the page from child state
        $scope.filteredStudies = DatasetService.filterStudies($scope.studiesWithDetail, filterSelections);
      }

      function toggleFilterOptions() {
        $scope.showFilterOptions = !$scope.showFilterOptions;
      }

      function showStudyDialog() {
        $modal.open({
          templateUrl: './createStudy.html',
          scope: $scope,
          controller: 'CreateStudyController',
          resolve: {
            successCallback: function() {
              return function(newVersion) {
                $state.go('versionedDataset', {
                  userUid: $stateParams.userUid,
                  datasetUuid: $stateParams.datasetUuid,
                  versionUuid: newVersion
                });
              };
            }
          }
        });
      }

      function showDeleteStudyDialog(study) {
        $modal.open({
          templateUrl: './deleteStudy.html',
          scope: $scope,
          controller: 'DeleteStudyController',
          windowClass: 'small',
          resolve: {
            successCallback: function() {
              return function(newVersion) {
                $state.go('versionedDataset', {
                  userUid: $stateParams.userUid,
                  datasetUuid: $stateParams.datasetUuid,
                  versionUuid: newVersion
                });
              };
            },
            study: function() {
              return study;
            }
          }
        });
      }

      function createProjectDialog() {
        $modal.open({
          templateUrl: '../project/createProjectModal.html',
          controller: 'CreateProjectModalController',
          resolve: {
            callback: function() {
              return function(newProject) {
                $state.go('project', {
                  projectId: newProject.id
                });
              };
            },
            dataset: function() {
              return {
                title: $scope.dataset.title,
                description: $scope.dataset.comment,
                headVersion: $scope.currentRevision.uri,
                datasetUuid: $scope.dataset.datasetUuid
              };
            }
          }
        });
      }

      function showEditDatasetModal() {
        $modal.open({
          templateUrl: './editDataset.html',
          controller: 'EditDatasetController',
          resolve: {
            dataset: function() {
              return $scope.dataset;
            },
            userUid: function() {
              return $scope.userUid;
            },
            callback: function() {
              return function(newTitle, newDescription) {
                $scope.dataset.title = newTitle;
                $scope.dataset.comment = newDescription;
                loadHistory();
              };
            }
          }
        });
      }
    };
    return dependencies.concat(DatasetController);
  });
