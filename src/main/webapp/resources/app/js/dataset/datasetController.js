'use strict';
define(['rdfstore'],
 function(rdfstore) {
  var dependencies = ['$scope', '$stateParams', '$modal', 'DatasetService', 'DatasetResource',
    'StudiesWithDetailResource', 'JsonLdService'
  ];
  var DatasetController = function($scope, $stateParams, $modal, DatasetService, DatasetResource,
    StudiesWithDetailResource, JsonLdService) {

    var store,
      datasetQuery =
      'prefix ontology: <http://trials.drugis.org/ontology#>' +
      'prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
      'prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
      'prefix instance: <http://trials.drugis.org/instances/>' +
      'select' +
      ' ?label ?comment' +
      ' where {' +
      '    ?datasetUid' +
      '      rdf:type ontology:Dataset ;' +
      '      rdfs:label ?label ; ' +
      '      rdfs:comment ?comment . ' +
      '}';

    DatasetResource.get($stateParams, function(responce, status) {

      rdfstore.create(function(store) {
        store.load('text/n3', responce.n3Data, function(success, results) {

          store.execute(datasetQuery, function(success, results) {
            if (success) {
              $scope.dataset = results.length === 1 ? results[0] : console.error('single result expexted');
              $scope.dataset.uuid = $stateParams.datasetUUID;
              $scope.$apply(); // rdf store does not trigger apply
            } else {
              console.error('query failed!');
            }
          });

        });
      });
    });

    $scope.loadStudiesWithDetail = function() {
      StudiesWithDetailResource.get($stateParams).$promise.then(function(result) {
        $scope.studiesWithDetail = result['@graph'];
        $scope.studiesWithDetail = JsonLdService.rewriteAtIds($scope.studiesWithDetail);
        if (!$scope.studiesWithDetail) {
          $scope.studiesWithDetail = {};
        }
        $scope.studiesWithDetail.$resolved = true;
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