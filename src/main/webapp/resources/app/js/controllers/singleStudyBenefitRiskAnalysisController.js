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

    var hasMissingOutcomes = function(study) {
      return study.missingOutcomes && study.missingOutcomes.length > 0;
    };

    var hasMissingInterventions = function(study) {
      return study.missingInterventions && study.missingInterventions.length > 0;
    };

    $scope.validateAnalysis = function(analysis) {
      return analysis.selectedInterventions.length >= 2 &&
        analysis.selectedOutcomes.length >= 2 &&
        $scope.studyModel.selectedStudy &&
        !$scope.studyModel.selectedStudy.hasMatchedMixedTreatmentArm &&
        !hasMissingOutcomes($scope.studyModel.selectedStudy) &&
        !hasMissingInterventions($scope.studyModel.selectedStudy);
    };


    $scope.isValidAnalysis = $scope.validateAnalysis($scope.analysis);

    function outcomesChanged() {
      $scope.studies = SingleStudyBenefitRiskAnalysisService.addMissingOutcomesToStudies($scope.studies, $scope.analysis.selectedOutcomes);
      updateAnalysis();
    }

    function interventionsChanged() {
      $scope.studies = SingleStudyBenefitRiskAnalysisService.addMissingInterventionsToStudies($scope.studies, $scope.analysis.selectedInterventions);
      $scope.studies = SingleStudyBenefitRiskAnalysisService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.analysis.selectedInterventions);
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

      _.each(studies, function(study) {
        study.interventionUids = compileListOfInterventionUids(study);
      });

      $scope.studies = SingleStudyBenefitRiskAnalysisService.addMissingOutcomesToStudies($scope.studies, $scope.analysis.selectedOutcomes);
      $scope.studies = SingleStudyBenefitRiskAnalysisService.addMissingInterventionsToStudies($scope.studies, $scope.analysis.selectedInterventions);
      $scope.studies = SingleStudyBenefitRiskAnalysisService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.analysis.selectedInterventions);
    });

    function compileListOfInterventionUids(study) {
      var interventionUids = [];

      _.each(study.treatmentArms, function(treatmentArm) {
        interventionUids = interventionUids.concat(treatmentArm.interventionUids);
      });

      return interventionUids;
    }


    function updateAnalysis() {
      $scope.isValidAnalysis = $scope.validateAnalysis($scope.analysis);
      AnalysisResource.save($scope.analysis);
    }

    $scope.studyGroupFn = function(study) {
      return $scope.isValidStudy(study) ? 'Analysable studies' : 'Un-analysable Studies';
    };

    $scope.isValidStudy = function(study) {
      return SingleStudyBenefitRiskAnalysisService.isValidStudyOption(study);
    };

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
      SingleStudyBenefitRiskAnalysisService.getProblem($scope.analysis).then(function(problem) {
        $scope.analysis.problem = problem;
        AnalysisResource.save($scope.analysis).$promise.then(function() {
          $scope.goToDefaultScenarioView();
        });
      });
    };

  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisController);
});
