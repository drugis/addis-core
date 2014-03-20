'use strict';
define(['underscore'], function () {
  var dependencies = ['$scope', '$stateParams', '$q',
    'ProjectsService', 'AnalysisService', 'OutcomeService', 'InterventionService', 'Select2UtilService', 'TrialverseStudyService'
  ];
  var AnalysisController = function ($scope, $stateParams, $q,
    ProjectsService, AnalysisService, OutcomeService, InterventionService, Select2UtilService, TrialverseStudyService) {

    $scope.loading = {
      loaded: false
    };

    $scope.project = ProjectsService.get($stateParams);
    $scope.analysis = AnalysisService.get($stateParams);

    $scope.outcomes = OutcomeService.query($stateParams);
    $scope.interventions = InterventionService.query($stateParams);


    $scope.selectedOutcomeIds = [];
    $scope.selectedInterventionIds = [];

    $q.all([
      $scope.project.$promise,
      $scope.analysis.$promise
    ]).then(function () {
      $scope.loading.loaded = true;
      $scope.studies = TrialverseStudyService.query({
        id: $scope.project.trialverseId
      });
      $scope.selectedOutcomeIds = Select2UtilService.objectsToIds($scope.analysis.selectedOutcomes);
      $scope.selectedInterventionIds = Select2UtilService.objectsToIds($scope.analysis.selectedInterventions);

      $scope.$watchCollection('selectedOutcomeIds', function (newValue, oldValue) {
        if (newValue.length !== $scope.analysis.selectedOutcomes.length) {
          $scope.analysis.selectedOutcomes = Select2UtilService.idsToObjects($scope.selectedOutcomeIds, $scope.outcomes);
          $scope.analysis.$save();
        }
      });

      $scope.$watchCollection('selectedInterventionIds', function (newValue, oldValue) {
        if (newValue.length !== $scope.analysis.selectedInterventions.length) {
          $scope.analysis.selectedInterventions = Select2UtilService.idsToObjects($scope.selectedInterventionIds, $scope.interventions);
          $scope.analysis.$save();
        }
      });

      $scope.$watch('analysis.studyId', function (newValue, oldValue) {
        if (oldValue !== newValue) {
          $scope.analysis.$save();
        }
      });

    });

  };
  return dependencies.concat(AnalysisController);
});