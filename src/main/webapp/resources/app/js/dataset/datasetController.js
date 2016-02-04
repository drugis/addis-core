'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$q', '$window', '$location', '$stateParams', '$modal', '$filter', 'DatasetVersionedResource', 'StudiesWithDetailsService',
     'HistoryResource', 'ConceptService', 'VersionedGraphResource', 'DatasetResource', 'GraphResource'
    ];
    var DatasetController = function($scope, $q, $window, $location, $stateParams, $modal, $filter, DatasetVersionedResource, StudiesWithDetailsService,
       HistoryResource, ConceptService, VersionedGraphResource, DatasetResource, GraphResource) {

      // no version so this must be head view
      $scope.isHeadView = !$stateParams.versionUuid;
      if(!$scope.isHeadView) {
        $scope.versionUuid = $stateParams.versionUuid;
      }

      function isEditAllowedOnVersion() {
        return $scope.currentRevision && $scope.currentRevision.isHead;
      }

      function isEditingAllowed() {
        return !!($scope.dataset && $scope.dataset.creator === $window.config.user.userEmail &&
          isEditAllowedOnVersion());
      }

      $scope.loadConcepts = function() {
        // load the concepts data from the backend
        var getConceptsFromBackendDefer;
        if ($scope.versionUuid) {
          getConceptsFromBackendDefer = VersionedGraphResource.getConceptJson({
            userUid: $stateParams.userUid,
            datasetUUID: $stateParams.datasetUUID,
            graphUuid: 'concepts',
            versionUuid: $stateParams.versionUuid
          });
        } else {
          getConceptsFromBackendDefer = GraphResource.getConceptJson({
            userUid: $stateParams.userUid,
            datasetUUID: $stateParams.datasetUUID,
            graphUuid: 'concepts',
          });
        }

        // place loaded data into fontend cache and return a promise
        ConceptService.loadJson(getConceptsFromBackendDefer.$promise);
        return getConceptsFromBackendDefer.$promise;
      };

      $scope.userUid = $stateParams.userUid;
      $scope.datasetUUID = $stateParams.datasetUUID;
      $scope.versionUuid = $stateParams.versionUuid;

      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.userUid = $stateParams.userUid;
      $scope.isEditingAllowed = false;
      $scope.datasetConcepts = $scope.loadConcepts();

      function getJson(resource) {
        resource.getForJson($stateParams).$promise.then(function(response) {
          $scope.dataset = {
            datasetUri: $scope.datasetUUID,
            label: response['http://purl.org/dc/terms/title'],
            comment: response['http://purl.org/dc/terms/description'],
            creator: response['http://purl.org/dc/terms/creator']
          };
          $scope.isEditingAllowed = isEditingAllowed();
        });
      }

      if ($scope.isHeadView) {
        getJson(DatasetResource);
      } else {
        getJson(DatasetVersionedResource);
      }

      HistoryResource.query($stateParams).$promise.then(function(historyItems) {

        function segmentAfterLastSlash(str) {
          return str.substr(str.lastIndexOf('/') + 1);
        }

        if ($scope.isHeadView) {
          $scope.currentRevision = historyItems[historyItems.length - 1];
          $scope.currentRevision.isHead = true;
          $scope.versionUuid = segmentAfterLastSlash($scope.currentRevision.uri);
        } else {
          // sort to know iF curentRevission is head
          $scope.currentRevision = _.find(historyItems, function(item) {
            return item.uri.lastIndexOf($stateParams.versionUuid) > 0;
          });
          $scope.currentRevision.isHead = $scope.currentRevision.historyOrder === 0;
        }

        $scope.isEditingAllowed = isEditingAllowed();
      });

      $scope.loadStudiesWithDetail = function() {
        StudiesWithDetailsService.get($stateParams.userUid, $stateParams.datasetUUID, $stateParams.versionUuid)
          .then(function(result) {
            $scope.studiesWithDetail = result;
          });
      };

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

      $scope.showStudyDialog = function() {
        $modal.open({
          templateUrl: 'app/js/dataset/createStudy.html',
          scope: $scope,
          controller: 'CreateStudyController',
          resolve: {
            successCallback: function() {
              return function(newVersion) {
                $location.path('/users/' + $stateParams.userUid + '/datasets/' +
                  $stateParams.datasetUUID + '/versions/' + newVersion);
              };
            }
          }
        });
      };

      $scope.loadStudiesWithDetail();

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
        orderByField: 'name'
      };
    };
    return dependencies.concat(DatasetController);
  });
