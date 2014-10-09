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
      $scope.outcomes = concatWithNoDuplicates(outcomes, $scope.outcomes);
      $scope.$watchCollection('analysis.selectedOutcomes', analysisChanged);
    });

    InterventionResource.query(projectIdParam, function(interventions) {
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      $scope.interventions = concatWithNoDuplicates(interventions, $scope.interventions);
      $scope.$watchCollection('analysis.selectedInterventions', analysisChanged);
    });

    $scope.isValidAnalysis = false;
    $scope.errorMessage = {};
    $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);

    $scope.studyModel = {
      selectedStudy: {}
    };

    $scope.studies = TrialverseStudyResource.query({
      namespaceUid: $scope.project.namespaceUid
    }, function(studies) {
      $scope.studyModel.selectedStudy = _.find(studies, function(study) {
        return study.uid === $scope.analysis.studyUid;
      });
    });

    $scope.onStudySelect = function(item) {
      $scope.analysis.studyUid = item.uid;
      $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
      $scope.errorMessage = {};
      AnalysisResource.save($scope.analysis);
    };


    $scope.goToDefaultScenarioView = function() {
      SingleStudyBenefitRiskAnalysisService
        .getDefaultScenario()
        .then(function(scenario) {
          $state.go(DEFAULT_VIEW, {
            id: scenario.id
          });
        });
    };

    function analysisChanged(oldValue, newValue) {
      if (newValue.length !== oldValue.length) {
        $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
        $scope.errorMessage = {};
        // use AnalysisResource.save as to keep the binding on analysis.selectedOptions alive
        AnalysisResource.save($scope.analysis);
      }
    }

    function concatWithNoDuplicates(source, target) {
      var filtered = _.filter(source, function(sourceItem) {
        return !_.find(target, function(targetItem) {
          return targetItem.id === sourceItem.id;
        });
      });
      return filtered.concat(target);
    }

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