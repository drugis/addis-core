'use strict';
define(['underscore'], function() {
  var dependencies = ['$scope', '$stateParams', '$state', '$q', '$window',
    'OutcomeResource', 'InterventionResource', 'Select2UtilService', 'TrialverseStudyResource', 'ProblemResource',
    'SingleStudyBenefitRiskAnalysisService', 'DEFAULT_VIEW', 'AnalysisResource'
  ];
  var SingleStudyBenefitRiskAnalysisController = function($scope, $stateParams, $state, $q, $window,
    OutcomeResource, InterventionResource,
    Select2UtilService, TrialverseStudyResource, ProblemResource, SingleStudyBenefitRiskAnalysisService, DEFAULT_VIEW, AnalysisResource) {

    var projectIdParam = {
      projectId: $stateParams.projectId
    };

    $scope.outcomes = $scope.analysis.selectedOutcomes;
    $scope.interventions = $scope.analysis.selectedInterventions;
    
    OutcomeResource.query(projectIdParam, function(outcomes) {
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      angular.forEach(outcomes, function(outcome) {
        var found = _.find($scope.outcomes, function(outcomeOption) {
          return outcomeOption.id === outcome.id;
        });
        if (!found) {
          $scope.outcomes.push(outcome);
        }
      });
      $scope.$watchCollection('analysis.selectedOutcomes', function(oldValue, newValue) {
        if (newValue.length !== oldValue.length) {
          $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          // use AnalysisResource.save as to keep the binding on analysis.selectedOptions alive
          AnalysisResource.save($scope.analysis);
        }
      });
    });

    InterventionResource.query(projectIdParam, function(interventions) {
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      angular.forEach(interventions, function(intervention) {
        var found = _.find($scope.interventions, function(interventionOption) {
          return interventionOption.id === intervention.id;
        });
        if (!found) {
          $scope.interventions.push(intervention);
        }
      });
      $scope.$watchCollection('analysis.selectedInterventions', function(oldValue, newValue) {
        if (newValue.length !== oldValue.length) {
          $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          // use AnalysisResource.save as to keep the binding on analysis.selectedOptions alive
          AnalysisResource.save($scope.analysis);
        }
      });
    });

    $scope.isValidAnalysis = false;
    $scope.errorMessage = {};
    $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);

    $scope.studies = TrialverseStudyResource.query({
      namespaceUid: $scope.project.namespaceUid
    });

    $scope.$watch('analysis.studyUid', function(newValue, oldValue) {
      if (oldValue !== newValue) {
        $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
        $scope.errorMessage = {};
        AnalysisResource.save($scope.analysis);
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

  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisController);
});