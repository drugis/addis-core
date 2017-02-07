'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$window', '$location', '$stateParams', '$state', '$modal', '$filter',
      'DatasetVersionedResource', 'StudiesWithDetailsService', 'HistoryResource', 'ConceptsService',
      'VersionedGraphResource', 'DatasetResource', 'GraphResource', 'UserService', 'DataModelService'
    ];
    var DatasetController = function($scope, $window, $location, $stateParams, $state, $modal, $filter,
      DatasetVersionedResource, StudiesWithDetailsService, HistoryResource, ConceptsService,
      VersionedGraphResource, DatasetResource, GraphResource, UserService, DataModelService
    ) {

      $scope.createProjectDialog = createProjectDialog;
      $scope.showEditDatasetModal = showEditDatasetModal;
      $scope.showDeleteStudyDialog = showDeleteStudyDialog;
      $scope.userUid = $stateParams.userUid;
      $scope.datasetUuid = $stateParams.datasetUuid;
      // no version so this must be head view
      $scope.isHeadView = !$stateParams.versionUuid;
      if (!$scope.isHeadView) {
        $scope.versionUuid = $stateParams.versionUuid;
      }

      $scope.hasLoggedInUser = UserService.hasLoggedInUser();

      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.isEditingAllowed = false;
      loadStudiesWithDetail();
      $scope.datasetConcepts = loadConcepts().then(function(graph) {
        $scope.interventions = _.filter(graph['@graph'], ['@type', 'ontology:Drug']);
        $scope.variables = _.filter(graph['@graph'], ['@type', 'ontology:Variable']);
      });
      $scope.filter = {};

      if ($scope.isHeadView) {
        getJson(DatasetResource);
      } else {
        getJson(DatasetVersionedResource);
      }

      loadHistory();

      function loadHistory() {
        var historyCoords = {
          userUid: $stateParams.userUid,
          datasetUuid: $stateParams.datasetUuid
        };
        HistoryResource.query(historyCoords).$promise.then(function(historyItems) {
          if ($scope.isHeadView) {
            $scope.currentRevision = historyItems[historyItems.length - 1];
            $scope.currentRevision.isHead = true;
          } else {
            // sort to know iF curentRevission is head
            $scope.currentRevision = _.find(historyItems, function(item) {
              return item.uri.lastIndexOf($stateParams.versionUuid) > 0;
            });
            if ($scope.currentRevision.historyOrder === 0) {
              $scope.currentRevision.isHead = true;
              // turns out its the head verion now we have the version information
              $scope.isHeadView = true;
            } else {
              $scope.currentRevision.isHead = false;
            }
          }

          $scope.isEditingAllowed = isEditingAllowed();
        });
      }

      function isEditAllowedOnVersion() {
        return $scope.currentRevision && $scope.currentRevision.isHead;
      }

      function isEditingAllowed() {
        return !!($scope.dataset && UserService.isLoginUserEmail($scope.dataset.creator) && isEditAllowedOnVersion());
      }

      function loadConcepts() {
        // load the concepts data from the backend
        var conceptsPromise;
        if ($scope.versionUuid) {
          conceptsPromise = VersionedGraphResource.getConceptJson({
            userUid: $stateParams.userUid,
            datasetUuid: $stateParams.datasetUuid,
            graphUuid: 'concepts',
            versionUuid: $stateParams.versionUuid
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
        // place loaded data into fontend cache and return a promise
        return ConceptsService.loadJson(cleanedConceptsPromise);
      }

      function getJson(resource) {
        resource.getForJson($stateParams).$promise.then(function(response) {
          $scope.dataset = {
            datasetUuid: $scope.datasetUuid,
            title: response['http://purl.org/dc/terms/title'],
            comment: response['http://purl.org/dc/terms/description'],
            creator: response['http://purl.org/dc/terms/creator']
          };
          $scope.isEditingAllowed = isEditingAllowed();
        });
      }

      function loadStudiesWithDetail() {
        StudiesWithDetailsService.get($stateParams.userUid, $stateParams.datasetUuid, $stateParams.versionUuid)
          .then(function(result) {
            $scope.studiesWithDetail = result instanceof Array ? result : [];
            $scope.filteredStudies = $scope.studiesWithDetail;
          });
      }

      $scope.showTableOptions = function() {
        $modal.open({
          templateUrl: 'app/js/dataset/tableOptions.html',
          scope: $scope,
          controller: function($scope, $modalInstance) {
            $scope.cancel = function() {
              $modalInstance.dismiss('cancel');
            };
          }
        });
      };

      $scope.showFilterOptions = function() {
        $modal.open({
          templateUrl: 'app/js/dataset/filterOptions.html',
          scope: $scope,
          controller: 'FilterDatasetController',
          resolve: {
            callback: function() {
              return function(drugFilters, variableFilters, filteredStudies) {
                $scope.drugFilters = drugFilters;
                $scope.variableFilters = variableFilters;
                $scope.filteredStudies = filteredStudies;
              };
            }
          }
        });
      };

      $scope.showStudyDialog = function() {
        $modal.open({
          templateUrl: 'app/js/dataset/createStudy.html',
          scope: $scope,
          controller: 'CreateStudyController',
          resolve: {
            successCallback: function() {
              return function(newVersion) {
                $location.path('/users/' + $stateParams.userUid + '/datasets/' +
                  $stateParams.datasetUuid + '/versions/' + newVersion);
              };
            }
          }
        });
      };

      function showDeleteStudyDialog(study) {
        $modal.open({
          templateUrl: 'app/js/dataset/deleteStudy.html',
          scope: $scope,
          controller: 'DeleteStudyController',
          windowClass: 'small',
          resolve: {
            successCallback: function() {
              return function(newVersion) {
                $location.path('/users/' + $stateParams.userUid + '/datasets/' +
                  $stateParams.datasetUuid + '/versions/' + newVersion);
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
          templateUrl: 'app/js/project/createProjectModal.html',
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
          templateUrl: 'app/js/dataset/editDataset.html',
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

      $scope.tableOptions = {
        columns: [{
          id: 'title',
          label: 'Title',
          visible: true
        }, {
          id: 'studySize',
          label: 'Study size',
          visible: true
        }, {
          id: 'indication',
          label: 'Indication',
          visible: false
        }, {
          id: 'status',
          label: 'Status',
          visible: true,
          type: 'removePreamble',
          frontStr: 'http://trials.drugis.org/ontology#Status'
        }, {
          id: 'allocation',
          label: 'Allocation',
          type: 'removePreamble',
          frontStr: 'http://trials.drugis.org/ontology#Allocation',
          visible: false
        }, {
          id: 'blinding',
          label: 'Blinding',
          type: 'removePreamble',
          frontStr: 'http://trials.drugis.org/ontology#',
          visible: false
        }, {
          id: 'drugNames',
          label: 'Investigational drugNames',
          visible: true
        }, {
          id: 'numberOfArms',
          label: 'Number of Arms',
          visible: false
        }, {
          id: 'publications',
          label: 'Publications links',
          visible: false,
          type: 'urlList'
        }, {
          id: 'doseType',
          label: 'Dosing',
          visible: false,
          type: 'dosing'
        }, {
          id: 'startDate',
          label: 'Start date',
          visible: false,
        }, {
          id: 'endDate',
          label: 'End date',
          visible: false,
        }],
        reverseSortOrder: false,
        orderByField: 'label'
      };
    };
    return dependencies.concat(DatasetController);
  });
