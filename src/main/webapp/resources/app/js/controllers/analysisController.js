'use strict';
define(['underscore'], function() {
  var dependencies = ['$scope', '$stateParams', '$state', '$q', '$window',
    'ProjectResource', 'OutcomeResource', 'InterventionResource',
    'Select2UtilService', 'TrialverseStudyResource', 'ProblemResource', 'AnalysisService', 'DEFAULT_VIEW',
    'currentAnalysis'
  ];
  var AnalysisController = function($scope, $stateParams, $state, $q, $window,
    ProjectResource, OutcomeResource, InterventionResource,
    Select2UtilService, TrialverseStudyResource, ProblemResource, AnalysisService, DEFAULT_VIEW,
    currentAnalysis) {

    $scope.$parent.loading = {
      loaded: false
    };

    $scope.editMode = {
      disableEditing: true
    };

    $scope.project = ProjectResource.get($stateParams);
    $scope.analysis = currentAnalysis;
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.interventions = InterventionResource.query($stateParams);
    $scope.selectedOutcomeIds = [];
    $scope.selectedInterventionIds = [];
    $scope.isProblemDefined = false;

    $q.all([
      $scope.project.$promise,
      $scope.analysis.$promise
    ]).then(function() {
      var userIsOwner;

      $scope.$parent.loading.loaded = true;
      $scope.$parent.project = $scope.project;
      userIsOwner = $window.config.user.id === $scope.project.owner.id;
      if($scope.analysis.problem){
        $scope.isProblemDefined = true;
      }
      $scope.editMode.disableEditing = !userIsOwner || $scope.isProblemDefined;

      $scope.select2Options = {
        'readonly': $scope.editMode.disableEditing
      };

      //  angular ui bug work-around, select2-ui does not properly watch for changes in the select2-options 
      $('#criteriaSelect').select2('readonly', $scope.editMode.disableEditing);
      $('#interventionsSelect').select2('readonly', $scope.editMode.disableEditing);

      $scope.studies = TrialverseStudyResource.query({
        id: $scope.project.trialverseId
      });
      $scope.selectedOutcomeIds = Select2UtilService.objectsToIds($scope.analysis.selectedOutcomes);
      $scope.selectedInterventionIds = Select2UtilService.objectsToIds($scope.analysis.selectedInterventions);

      $scope.$watchCollection('selectedOutcomeIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedOutcomes.length) {
          $scope.analysis.selectedOutcomes = Select2UtilService.idsToObjects($scope.selectedOutcomeIds, $scope.outcomes);
          $scope.analysis.$save();
        }
      });

      $scope.$watchCollection('selectedInterventionIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedInterventions.length) {
          $scope.analysis.selectedInterventions = Select2UtilService.idsToObjects($scope.selectedInterventionIds, $scope.interventions);
          $scope.analysis.$save();
        }
      });

      $scope.$watch('analysis.studyId', function(newValue, oldValue) {
        if (oldValue !== newValue) {
          $scope.analysis.$save();
        }
      });

      $scope.goToDefaultScenarioView = function() {
        AnalysisService
          .getDefaultScenario()
          .then(function(scenario) {
            $state.go(DEFAULT_VIEW, {scenarioId: scenario.id});
          });
      }

      $scope.createProblem = function() {
        AnalysisService
          .createProblem($scope.analysis)
          .then(function(scenario) {
            $state.go(DEFAULT_VIEW, {scenarioId: scenario.id});
          });
      };
    });
  };
  return dependencies.concat(AnalysisController);
});