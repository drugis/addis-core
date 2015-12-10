'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$q', '$window', '$location', '$stateParams', '$modal', '$filter',
      'SingleDatasetService', 'DatasetVersionedResource', 'StudiesWithDetailsService',
      'RemoteRdfStoreService', 'HistoryResource', 'ConceptService', 'VersionedGraphResource', 'DatasetResource'
    ];
    var DatasetController = function($scope, $q, $window, $location, $stateParams, $modal, $filter,
      SingleDatasetService, DatasetVersionedResource, StudiesWithDetailsService,
      RemoteRdfStoreService, HistoryResource, ConceptService, VersionedGraphResource, DatasetResource) {

      // no version so this must be head view
      $scope.isHeadView = !$stateParams.versionUuid;

      function isEditingAllowed() {
        return !!($scope.dataset && $scope.dataset.creator === $window.config.user.userEmail &&
          isEditAllowedOnVersion());
      }

      function isEditAllowedOnVersion() {
        return $scope.isHeadView || ($scope.currentRevision && $scope.currentRevision.isHead);
      }

      $scope.loadConcepts = function() {
        return ConceptService.loadJson(VersionedGraphResource.getJsonNoTransform({
          userUid: $stateParams.userUid,
          datasetUUID: $stateParams.datasetUUID,
          graphUuid: 'concepts',
          versionUuid: $stateParams.versionUuid
        }).$promise);
      };

      $scope.userUid = $stateParams.userUid;
      $scope.datasetUUID = $stateParams.datasetUUID;
      $scope.versionUuid = $stateParams.versionUuid;

      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.userUid = $stateParams.userUid;
      $scope.isEditingAllowed = false;
      //    $scope.datasetConcepts = $scope.loadConcepts();

      if ($scope.isHeadView) {
        DatasetResource.getForJson($stateParams, function(response) {
          $scope.dataset = {
            datasetUri: $scope.datasetUUID,
            label: response['http://purl.org/dc/terms/title'],
            comment: response['http://purl.org/dc/terms/description'],
            creator: response['http://purl.org/dc/terms/creator']
          };
          $scope.isEditingAllowed = isEditingAllowed();
        });
      } else {
        DatasetVersionedResource.get($stateParams, function(response) {
          $scope.dataset = {
            datasetUri: $scope.datasetUUID,
            label: response['http://purl.org/dc/terms/title'],
            comment: response['http://purl.org/dc/terms/description'],
            creator: response['http://purl.org/dc/terms/creator']
          };
          $scope.isEditingAllowed = isEditingAllowed();
        });
      }

      HistoryResource.query($stateParams).$promise.then(function(historyItems) {

        if (!$scope.isHeadView) {
          // sort to know it curentRevission is head
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
                $scope.reloadDatasets();
              }
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