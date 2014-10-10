'use strict';
define(['underscore'], function() {
  var dependencies = ['$scope', '$stateParams', '$state', '$q', '$window',
    'OutcomeResource', 'InterventionResource', 'TrialverseStudyResource', 'ProblemResource',
    'SingleStudyBenefitRiskAnalysisService', 'DEFAULT_VIEW', 'AnalysisResource'
  ];
  var SingleStudyBenefitRiskAnalysisController = function($scope, $stateParams, $state, $q, $window,
    OutcomeResource, InterventionResource, TrialverseStudyResource, ProblemResource, SingleStudyBenefitRiskAnalysisService, DEFAULT_VIEW, AnalysisResource) {

    var projectIdParam = {
      projectId: $stateParams.projectId
    };

    var projectNamespaceUid = {
      namespaceUid: $scope.project.namespaceUid
    };

    var isIdEqual = function(left, right) {
      return left.id === right.id;
    };

    $scope.outcomes = $scope.analysis.selectedOutcomes;
    $scope.interventions = $scope.analysis.selectedInterventions;
    $scope.studyModel = {
      selectedStudy: {}
    };
    $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);

    function outcomesChanged() {
      $scope.studies = getStudiesWithMissingOutcomes($scope.studies);
      updateAnalysis();
    }

    function interventionsChanged() {
      $scope.studies = getStudiesWithMissingInterventions($scope.studies);
      updateAnalysis();
    }

    OutcomeResource.query(projectIdParam).$promise.then(function(outcomes) {
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      $scope.outcomes = SingleStudyBenefitRiskAnalysisService.concatWithNoDuplicates(outcomes, $scope.outcomes, isIdEqual);
      $scope.$watchCollection('analysis.selectedOutcomes', function(oldValue, newValue) {
        if (newValue.length !== oldValue.length) {
          outcomesChanged();
        }
      });
    });

    InterventionResource.query(projectIdParam).$promise.then(function(interventions) {
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      $scope.interventions = SingleStudyBenefitRiskAnalysisService.concatWithNoDuplicates(interventions, $scope.interventions, isIdEqual);
      $scope.$watchCollection('analysis.selectedInterventions', function(oldValue, newValue) {
        if (newValue.length !== oldValue.length) {
          interventionsChanged();
        }
      });
    });

    $scope.studies = [];
    TrialverseStudyResource.query(projectNamespaceUid).$promise.then(function(studies) {
      $scope.studies = studies;
      $scope.studyModel.selectedStudy = _.find(studies, function(study) {
        return study.uid === $scope.analysis.studyUid;
      });
      $scope.studies = getStudiesWithMissingOutcomes($scope.studies);
      $scope.studies = getStudiesWithMissingInterventions($scope.studies);
    });

    function isSameOutcome(studyOutcomeUri, selectedOutcome) {
      var lastIndexOfSlash = studyOutcomeUri.lastIndexOf('/');
      var idPart = studyOutcomeUri.substring(lastIndexOfSlash + 1);
      return selectedOutcome.semanticOutcomeUri === idPart;
    }

    function isSameIntervention(studyOutcomeUri, selectedOutcome) {
      var lastIndexOfSlash = studyOutcomeUri.lastIndexOf('/');
      var idPart = studyOutcomeUri.substring(lastIndexOfSlash + 1);
      return selectedOutcome.semanticInterventionUri === idPart;
    }

    function getStudiesWithMissingOutcomes(studies) {
      return _.map(studies, function(study) {
        study.missingOutcomes = SingleStudyBenefitRiskAnalysisService.findMissing(
          $scope.analysis.selectedOutcomes, study.outcomeUids, isSameOutcome);
        return study;
      });
    }

    function getStudiesWithMissingInterventions(studies) {
      return _.map(studies, function(study) {
        study.missingInterventions = SingleStudyBenefitRiskAnalysisService.findMissing(
          $scope.analysis.selectedInterventions, study.interventionUids, isSameIntervention);
        return study;
      });
    }

    function updateAnalysis() {
      $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
      AnalysisResource.save($scope.analysis);
    }

    $scope.onStudySelect = function(item) {
      $scope.analysis.studyUid = item.uid;
      updateAnalysis();
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

    $scope.createProblem = function() {
      SingleStudyBenefitRiskAnalysisService.getProblem($scope.analysis)
        .then(function(problem) {
          $scope.analysis.problem = problem;
          AnalysisResource.save($scope.analysis).$promise
            .then(SingleStudyBenefitRiskAnalysisService.getDefaultScenario)
            .then(function(scenario) {
              $state.go(DEFAULT_VIEW, {
                id: scenario.id
              });
            });
        });
    };

  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisController);
});