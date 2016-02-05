'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$state', '$window',
    'currentAnalysis', 'currentProject',
    'OutcomeResource', 'InterventionResource', 'TrialverseStudyResource',
    'SingleStudyBenefitRiskAnalysisService', 'DEFAULT_VIEW', 'AnalysisResource'
  ];
  var SingleStudyBenefitRiskAnalysisController = function($scope, $stateParams, $state, $window,
   currentAnalysis, currentProject, OutcomeResource, InterventionResource, TrialverseStudyResource, SingleStudyBenefitRiskAnalysisService, DEFAULT_VIEW, AnalysisResource) {

    var deregisterOutcomeWatch, deregisterInterventionWatch;
    $scope.$parent.loading = {
      loaded: true
    };
    $scope.$parent.analysis = currentAnalysis;
    $scope.$parent.project = currentProject;
    // for mcda use
    $scope.workspace = $scope.analysis;
    $scope.project = currentProject;

    $scope.studies = [];
    $scope.outcomes = $scope.analysis.selectedOutcomes;
    $scope.interventions = $scope.analysis.selectedInterventions;
    $scope.studyModel = {
      selectedStudy: {}
    };
    $scope.isProblemDefined = !!currentAnalysis.problem;
    $scope.editMode = {
      isUserOwner: $window.config.user.id === currentProject.owner.id,
    };
    $scope.editMode.disableEditing = !$scope.editMode.isUserOwner || $scope.isProblemDefined;
    $scope.userId = $stateParams.userUid;

    var projectIdParam = {
      projectId: $stateParams.projectId
    };

    var projectNamespaceUid = {
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    };

    var isIdEqual = function(left, right) {
      return left.id === right.id;
    };

    var hasMissingOutcomes = function(study) {
      return study.missingOutcomes && study.missingOutcomes.length > 0;
    };

    var hasMissingInterventions = function(study) {
      return study.missingInterventions && study.missingInterventions.length > 0;
    };

    $scope.isValidAnalysis = function(analysis) {
      var twoOrMoreInerventions = analysis.selectedInterventions.length >= 2;
      var twoOrMoreOutcomes = analysis.selectedOutcomes.length >= 2;
      var noMatchedMixedTreatmentArm = $scope.studyModel.selectedStudy && !$scope.studyModel.selectedStudy.hasMatchedMixedTreatmentArm;
      var noMissingOutcomes = $scope.studyModel.selectedStudy && !hasMissingOutcomes($scope.studyModel.selectedStudy);
      var noMissingInterventions = $scope.studyModel.selectedStudy && !hasMissingInterventions($scope.studyModel.selectedStudy);

      var result = twoOrMoreInerventions && twoOrMoreOutcomes && noMatchedMixedTreatmentArm && noMissingOutcomes && noMissingInterventions;
      return result;
    };

    function outcomesChanged() {
      SingleStudyBenefitRiskAnalysisService.addMissingOutcomesToStudies($scope.studies, $scope.analysis.selectedOutcomes);
      SingleStudyBenefitRiskAnalysisService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.analysis.selectedInterventions);
      SingleStudyBenefitRiskAnalysisService.recalculateGroup($scope.studies);

      // necessary because angular-select uses $watchcollection instead of $watch
      $scope.studies.push({
        key: 'dirtyElement'
      });
      saveAnalysis();
    }

    function interventionsChanged() {
      SingleStudyBenefitRiskAnalysisService.addMissingInterventionsToStudies($scope.studies, $scope.analysis.selectedInterventions);
      SingleStudyBenefitRiskAnalysisService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.analysis.selectedInterventions);
      SingleStudyBenefitRiskAnalysisService.recalculateGroup($scope.studies);

      // necessary because angular-select uses $watchcollection instead of $watch
      $scope.studies.push({
        key: 'dirtyElement'
      });
      saveAnalysis();
    }

    OutcomeResource.query(projectIdParam).$promise.then(function(outcomes) {
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      $scope.outcomes = SingleStudyBenefitRiskAnalysisService.concatWithNoDuplicates(outcomes, $scope.outcomes, isIdEqual);
      deregisterOutcomeWatch = $scope.$watchCollection('analysis.selectedOutcomes', function(oldValue, newValue) {
        if (newValue.length !== oldValue.length) {
          outcomesChanged();
        }
      });
    });

    InterventionResource.query(projectIdParam).$promise.then(function(interventions) {
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      $scope.interventions = SingleStudyBenefitRiskAnalysisService.concatWithNoDuplicates(interventions, $scope.interventions, isIdEqual);
      deregisterInterventionWatch = $scope.$watchCollection('analysis.selectedInterventions', function(oldValue, newValue) {
        if (newValue.length !== oldValue.length) {
          interventionsChanged();
        }
      });
    });

    TrialverseStudyResource.query(projectNamespaceUid).$promise.then(function(studies) {
      $scope.studies = studies;
      $scope.studyArrayLength = studies.length;

      $scope.studyModel.selectedStudy = _.find(studies, function(study) {
        return study.studyGraphUid === $scope.analysis.studyGraphUid;
      });

      _.each(studies, function(study) {
        study.interventionUids = compileListOfInterventionUids(study);
      });

      SingleStudyBenefitRiskAnalysisService.addMissingOutcomesToStudies($scope.studies, $scope.analysis.selectedOutcomes);
      SingleStudyBenefitRiskAnalysisService.addMissingInterventionsToStudies($scope.studies, $scope.analysis.selectedInterventions);
      SingleStudyBenefitRiskAnalysisService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.analysis.selectedInterventions);
      SingleStudyBenefitRiskAnalysisService.recalculateGroup($scope.studies);
    });

    function compileListOfInterventionUids(study) {
      var interventionUids = [];

      _.each(study.treatmentArms, function(treatmentArm) {
        interventionUids = interventionUids.concat(treatmentArm.interventionUids);
      });

      return interventionUids;
    }

    function saveAnalysis() {
      AnalysisResource.save($scope.analysis, function() {
        // necessary because angular-select uses $watchcollection instead of $watch
        $scope.studies = $scope.studies.splice(0, $scope.studyArrayLength);
      });
    }

    $scope.onStudySelect = function(item) {
      $scope.analysis.studyGraphUid = item.studyGraphUid;
      saveAnalysis();
    };

    $scope.goToDefaultScenarioView = function() {
      SingleStudyBenefitRiskAnalysisService
        .getDefaultScenario()
        .then(function(scenario) {
          $state.go(DEFAULT_VIEW, {
            userUid: $scope.userId,
            id: scenario.id
          });
        });
    };

    $scope.createProblem = function() {
      if(deregisterOutcomeWatch) {
        deregisterOutcomeWatch();
      }
      if(deregisterInterventionWatch) {
        deregisterInterventionWatch();
      }
      SingleStudyBenefitRiskAnalysisService.getProblem($scope.analysis).then(function(problem) {
        $scope.analysis.problem = problem;
        AnalysisResource.save($scope.analysis).$promise.then(function(response) {
          $scope.analysis = response;
          $scope.workspace = response;
          $scope.goToDefaultScenarioView();
        });
      });
    };

  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisController);
});
