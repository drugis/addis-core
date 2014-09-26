'use strict';
define(['underscore'], function() {
  var dependencies = ['$scope', '$stateParams', '$state', '$q', '$window',
    'OutcomeResource', 'InterventionResource','Select2UtilService', 'TrialverseStudyResource', 'ProblemResource',
     'SingleStudyBenefitRiskAnalysisService', 'DEFAULT_VIEW'
  ];
  var SingleStudyBenefitRiskAnalysisController = function($scope, $stateParams, $state, $q, $window,
    OutcomeResource, InterventionResource,
    Select2UtilService, TrialverseStudyResource, ProblemResource, SingleStudyBenefitRiskAnalysisService, DEFAULT_VIEW) {

    var projectIdParam = {
      projectId: $stateParams.projectId
    };

    var outcomes = OutcomeResource.query(projectIdParam);
    var interventions = InterventionResource.query(projectIdParam);

    var initialiseOutcomes = function(outcomes) {
      $scope.outcomes = outcomes;
      $scope.selectedOutcomeIds = Select2UtilService.objectsToIds($scope.analysis.selectedOutcomes);
      $scope.$watchCollection('selectedOutcomeIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedOutcomes.length) {
          $scope.analysis.selectedOutcomes = Select2UtilService.idsToObjects($scope.selectedOutcomeIds, $scope.outcomes);
          $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          $scope.analysis.$save();
        }
      });
    };

    var initialiseInterventions = function(interventions) {
      $scope.interventions = interventions;
      $scope.selectedInterventionIds = Select2UtilService.objectsToIds($scope.analysis.selectedInterventions);
      $scope.$watchCollection('selectedInterventionIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedInterventions.length) {
          $scope.analysis.selectedInterventions = Select2UtilService.idsToObjects($scope.selectedInterventionIds, $scope.interventions);
          $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          $scope.analysis.$save();
        }
      });
    };

    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.isValidAnalysis = false;
    $scope.errorMessage = {};

    $q.all([$scope.analysis.$promise, $scope.project.$promise]).then(function() {
      $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);

      $scope.select2Options = {
        'readonly': $scope.$parent.editMode.disableEditing
      };

      //  angular ui bug work-around, select2-ui does not properly watch for changes in the select2-options 
      $('#criteriaSelect')
        .select2()
        .select2('readonly', $scope.$parent.editMode.disableEditing);
      $('#interventionsSelect')
        .select2()
        .select2('readonly', $scope.$parent.editMode.disableEditing);

      $scope.studies = TrialverseStudyResource.query({
        namespaceUid: $scope.project.namespaceUid
      });

      outcomes.$promise.then(initialiseOutcomes);
      interventions.$promise.then(initialiseInterventions);

      $scope.$watch('analysis.studyUid', function(newValue, oldValue) {
        if (oldValue !== newValue) {
          $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          $scope.analysis.$save();
        }
      });

      $scope.goToDefaultScenarioView = function() {
        SingleStudyBenefitRiskAnalysisService
          .getDefaultScenario()
          .then(function(scenario) {
            $state.go(DEFAULT_VIEW, {
              id: scenario.id
            });
          });
      };
      $scope.createProblem = function() {
        SingleStudyBenefitRiskAnalysisService.getProblem($scope.analysis)
          .then(function(problem) {
            if (SingleStudyBenefitRiskAnalysisService.validateProblem($scope.analysis, problem)) {
              $scope.analysis.problem = problem;
              $scope.analysis.$save()
                .then(SingleStudyBenefitRiskAnalysisService.getDefaultScenario)
                .then(function(scenario) {
                  $state.go(DEFAULT_VIEW, {
                    id: scenario.id
                  });
                });
            } else {
              $scope.errorMessage = {
                text: 'The selected study and the selected citeria/alternatives do not match.'
              };
            }
          });
      };
    });


  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisController);
});