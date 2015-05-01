'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$q', '$window','$stateParams', '$modal', '$filter',
     'DatasetService', 'DatasetVersionedResource','StudiesWithDetailsService',
     'JsonLdService', 'RemoteRdfStoreService', 'HistoryResource', 'HistoryService',
     'ConceptService', 'VersionedGraphResource'
    ];
    var DatasetController = function($scope, $q, $window, $stateParams, $modal, $filter,
     DatasetService, DatasetVersionedResource, StudiesWithDetailsService, JsonLdService,
     RemoteRdfStoreService, HistoryResource, HistoryService, ConceptService, VersionedGraphResource) {

      $scope.userUid = $stateParams.userUid;

      function isEditingAllowed() {
        return !!($scope.dataset && $scope.dataset.creator === $window.config.user.userEmail &&
          $scope.currentRevision && $scope.currentRevision.idx === 0);
      }

      $scope.isEditingAllowed = false;

      DatasetVersionedResource.get($stateParams, function(response) {
        DatasetService.reset();
        DatasetService.loadStore(response.data).then(function() {
          DatasetService.queryDataset().then(function(queryResult) {
            $scope.dataset = queryResult[0];
            $scope.dataset.uuid = $stateParams.datasetUUID;
            $scope.isEditingAllowed = isEditingAllowed();
          });
        });
      });

      HistoryResource.query($stateParams).$promise.then(function(historyItems) {
        // sort to know it curentRevission is head
        var indexedHistoryItems = HistoryService.addOrderIndex(historyItems);
        $scope.currentRevision = _.find(indexedHistoryItems, function(item) {
          return item['@id'].lastIndexOf($stateParams.versionUuid) > 0;
        });
        $scope.isEditingAllowed = isEditingAllowed();
      });

      // load concepts
      $scope.datasetConcepts = VersionedGraphResource.get({
        datasetUUID: $stateParams.datasetUUID,
        graphUuid: 'concepts',
        versionUuid: $stateParams.versionUuid
      }).$promise.then(function(conceptsTurtle) {
        return ConceptService.loadStore(conceptsTurtle.data).then(function() {
          return ConceptService.queryItems($stateParams.datasetUUID);
        });
      });

      $scope.loadStudiesWithDetail = function() {
        StudiesWithDetailsService.get($stateParams.datasetUUID, $stateParams.versionUuid).then(function(result) {
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
          controller: 'CreateStudyController'
        });
      };

      $scope.loadStudiesWithDetail();

      $scope.stripFrontFilter = $filter('stripFrontFilter');

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
          frontStr: 'http://trials.drugis.org/ontology#status'
        }, {
          id: 'allocation',
          label: 'Allocation',
          type: 'removePreamble',
          frontStr: 'http://trials.drugis.org/ontology#allocation',
          visible: false
        }, {
          id: 'blinding',
          label: 'Blinding',
          type: 'removePreamble',
          frontStr: 'http://trials.drugis.org/ontology#blinding',
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