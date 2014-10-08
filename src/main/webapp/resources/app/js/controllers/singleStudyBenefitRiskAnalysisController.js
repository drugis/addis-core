'use strict';
define(['underscore'], function() {
  var dependencies = ['$scope', '$stateParams', '$state', '$q', '$window',
    'OutcomeResource', 'InterventionResource', 'Select2UtilService', 'TrialverseStudyResource', 'ProblemResource',
    'SingleStudyBenefitRiskAnalysisService', 'DEFAULT_VIEW'
  ];
  var SingleStudyBenefitRiskAnalysisController = function($scope, $stateParams, $state, $q, $window,
    OutcomeResource, InterventionResource,
    Select2UtilService, TrialverseStudyResource, ProblemResource, SingleStudyBenefitRiskAnalysisService, DEFAULT_VIEW) {

    var projectIdParam = {
      projectId: $stateParams.projectId
    };

    $scope.availablePersons = [{
      name: 'Gert',
      age: 12
    }, {
      name: 'Connor',
      age: 13
    }, {
      name: 'Bob',
      age: 14
    }, {
      name: 'Joel',
      age: 15
    }, {
      name: 'Daan',
      age: 10
    }];
    $scope.persons = {
      mySelected: [$scope.availablePersons[2]]
    };

    $scope.outcomes = OutcomeResource.query(projectIdParam, function() {
      $scope.$watchCollection('selectedOutcomeIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedOutcomes.length) {
          $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          $scope.analysis.$save();
        }
      });
    });

    $scope.interventions = InterventionResource.query(projectIdParam, function() {
      $scope.selectedInterventionIds = Select2UtilService.objectsToIds($scope.analysis.selectedInterventions);
      $scope.$watchCollection('selectedInterventionIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedInterventions.length) {
          $scope.isValidAnalysis = SingleStudyBenefitRiskAnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          $scope.analysis.$save();
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

  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisController);
});