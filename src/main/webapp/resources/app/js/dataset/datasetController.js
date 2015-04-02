'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$window','$stateParams', '$modal', '$filter', 'DatasetService', 'DatasetVersionedResource',
      'StudiesWithDetailsService', 'JsonLdService', 'RemoteRdfStoreService', 'HistoryResource', 'HistoryService'
    ];
    var DatasetController = function($scope, $window, $stateParams, $modal, $filter, DatasetService, DatasetVersionedResource,
      StudiesWithDetailsService, JsonLdService, RemoteRdfStoreService, HistoryResource, HistoryService) {

      console.log('creating dataset controller');

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
          id: 'ontology:studySize',
          label: 'Study size',
          visible: true
        }, {
          id: 'hasIndicationLabel',
          label: 'Indication',
          visible: false
        }, {
          id: 'status',
          label: 'Status',
          visible: true,
          type: 'removePreamble',
          frontStr: 'status:'
        }, {
          id: 'has_allocation',
          label: 'Allocation',
          type: 'removePreamble',
          frontStr: 'ontology:allocation',
          visible: false
        }, {
          id: 'has_blinding',
          label: 'Blinding',
          type: 'removePreamble',
          frontStr: 'ontology:blinding',
          visible: false
        }, {
          id: 'usesDrugs',
          label: 'Investigational drugNames',
          visible: true
        }, {
          id: 'ontology:numberOfArms',
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
          id: 'has_start_date',
          label: 'Start date',
          visible: false,
        }, {
          id: 'has_end_date',
          label: 'End date',
          visible: false,
        }],
        reverseSortOrder: false,
        orderByField: 'name'
      };
    };
    return dependencies.concat(DatasetController);
  });