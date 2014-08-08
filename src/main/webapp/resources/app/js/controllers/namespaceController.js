'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'TrialverseResource', 'TrialverseStudiesWithDetailsResource'];
  var NamespaceController = function($scope, $stateParams, TrialverseResource, TrialverseStudiesWithDetailsResource) {
    $scope.namespace = TrialverseResource.get($stateParams);
    $scope.studiesWithDetails = TrialverseStudiesWithDetailsResource.get($stateParams);
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
        visible: true
      }, {
        id: 'status',
        label: 'Status',
        visible: true
      }, {
        id: 'allocation',
        label: 'Allocation',
        visible: false
      }, {
        id: 'blinding',
        label: 'Blinding',
        visible: false
      }, {
        id: 'investigationalDrugNames',
        label: 'Investigational drugNames',
        visible: true
      }, {
        id: 'numberOfArms',
        label: 'Number of Arms',
        visible: false
      }, {
        id: 'pubmedUrls',
        label: 'Publications links',
        visible: false,
        type: 'urlList'
      }],
      reverseSortOrder: false,
      orderByField: 'name'
    };
  };
  return dependencies.concat(NamespaceController);
});