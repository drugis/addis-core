'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modal', 'DatasetResource',
      'StudiesWithDetailResource', 'UUIDService', 'StudyService', 'StudyResource'
    ];
    var DatasetController = function($scope, $stateParams, $modal, DatasetResource,
      StudiesWithDetailResource, UUIDService, StudyService, StudyResource) {
      DatasetResource.get($stateParams).$promise.then(function(result) {
        $scope.datasetJSON = result;
        $scope.dataset = result['@graph'][0];
      });

      function loadStudiesWithDetail() {
        StudiesWithDetailResource.get($stateParams).$promise.then(function(result) {
          $scope.studiesWithDetail = result['@graph'];
          if (!$scope.studiesWithDetail) {
            $scope.studiesWithDetail = {};
          }
          $scope.studiesWithDetail.$resolved = true;
        });
      }

      function addStudyToDatasetGraph(studyUUID, datasetGraph) {
        // left-associated concat needed because it's not a list if dataset only
        // contains one study
        var newStudyURI = 'http://trials.drugis.org/studies/' + studyUUID;
        datasetGraph['@graph'][0].contains_study = newStudyURI;
        return datasetGraph;
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

      $scope.showStudyDialog = function() {
        $modal.open({
          templateUrl: 'app/js/dataset/createStudy.html',
          scope: $scope,
          controller: function($scope, $modalInstance) {
            $scope.createStudy = function(study) {
              var uuid = UUIDService.generate();
              var newStudy = StudyService.createEmptyStudyJsonLD(uuid, study);
              StudyResource.put({
                datasetUUID: $stateParams.datasetUUID,
                studyUUID: uuid
              }, newStudy).$promise.then(function() {
                $scope.datasetJSON = addStudyToDatasetGraph(uuid, $scope.datasetJSON);
                DatasetResource.save({
                  datasetUUID: $stateParams.datasetUUID
                }, $scope.datasetJSON).$promise.then(function() {
                  loadStudiesWithDetail();
                  $modalInstance.close();
                });
              });
            };

            $scope.cancel = function() {
              $modalInstance.dismiss('cancel');
            };
          }
        });
      };

      loadStudiesWithDetail();

      $scope.tableOptions = {
        columns: [{
          id: 'comment',
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