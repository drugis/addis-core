'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['$scope', '$stateParams', '$state',
    'currentAnalysis',
    'currentProject',
    'OutcomeResource',
    'InterventionResource',
    'SingleStudyBenefitRiskService',
    'DEFAULT_VIEW',
    'AnalysisResource',
    'ProjectStudiesResource',
    'UserService',
  ];
  var SingleStudyBenefitRiskController = function($scope, $stateParams, $state,
    currentAnalysis,
    currentProject,
    OutcomeResource,
    InterventionResource,
    SingleStudyBenefitRiskService,
    DEFAULT_VIEW,
    AnalysisResource,
    ProjectStudiesResource,
    UserService
  ) {

    $scope.analysis = currentAnalysis;
    var deregisterOutcomeWatch, deregisterInterventionWatch;
    $scope.$parent.loading = {
      loaded: true
    };
    $scope.studyModel = {
      selectedStudy: {}
    };

    var isUserOwner = UserService.isLoginUserId(currentProject.owner.id);
    $scope.editMode = {
      isUserOwner: isUserOwner
    };
    $scope.userId = $stateParams.userUid;
    $scope.isProblemDefined = !!currentAnalysis.problem;
    $scope.editMode.disableEditing = !$scope.editMode.isUserOwner || $scope.isProblemDefined || $scope.analysis.archived;
    $scope.studies = [];
    $scope.$parent.analysis = currentAnalysis;
    $scope.$parent.project = currentProject;
    // for mcda use
    $scope.workspace = $scope.analysis;
    $scope.project = currentProject;
    $scope.outcomes = $scope.analysis.selectedOutcomes;
    $scope.interventions = $scope.analysis.interventionInclusions;
    checkDuplicateOutcomes();


    var projectIdParam = {
      projectId: $stateParams.projectId
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
      var twoOrMoreInerventions = analysis.interventionInclusions.length >= 2;
      var twoOrMoreOutcomes = analysis.selectedOutcomes.length >= 2;
      var noMatchedMixedTreatmentArm = $scope.studyModel.selectedStudy && !$scope.studyModel.selectedStudy.hasMatchedMixedTreatmentArm;
      var noMissingOutcomes = $scope.studyModel.selectedStudy && !hasMissingOutcomes($scope.studyModel.selectedStudy);
      var noMissingInterventions = $scope.studyModel.selectedStudy && !hasMissingInterventions($scope.studyModel.selectedStudy);

      var result = twoOrMoreInerventions && twoOrMoreOutcomes && noMatchedMixedTreatmentArm && noMissingOutcomes && noMissingInterventions;
      return result;
    };

    function checkDuplicateOutcomes() {
      var outcomesByUri = _.groupBy($scope.analysis.selectedOutcomes, 'semanticOutcomeUri');
      $scope.duplicateOutcomesList = _.filter(outcomesByUri, function(outcomeByUri) {
        return outcomeByUri.length > 1;
      });
    }

    function outcomesChanged() { // Criteria
      checkDuplicateOutcomes();
      $scope.studies = SingleStudyBenefitRiskService.addMissingOutcomesToStudies($scope.studies, $scope.analysis.selectedOutcomes);
      SingleStudyBenefitRiskService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.analysis.interventionInclusions);
      SingleStudyBenefitRiskService.recalculateGroup($scope.studies);

      // necessary because angular-select uses $watchcollection instead of $watch
      $scope.studies.push({
        key: 'dirtyElement'
      });
      saveAnalysis();
    }

    function interventionsChanged() {
      $scope.studies = SingleStudyBenefitRiskService.addMissingInterventionsToStudies($scope.studies, $scope.analysis.interventionInclusions);
      SingleStudyBenefitRiskService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.analysis.interventionInclusions);
      $scope.studies = SingleStudyBenefitRiskService.addOverlappingInterventionsToStudies($scope.studies, $scope.analysis.interventionInclusions);
      SingleStudyBenefitRiskService.recalculateGroup($scope.studies);

      // necessary because angular-select uses $watchcollection instead of $watch
      $scope.studies.push({
        key: 'dirtyElement'
      });
      saveAnalysis();
    }

    OutcomeResource.query(projectIdParam).$promise.then(function(outcomes) {
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      $scope.outcomes = SingleStudyBenefitRiskService.concatWithNoDuplicates(outcomes, $scope.outcomes, isIdEqual);
      deregisterOutcomeWatch = $scope.$watchCollection('analysis.selectedOutcomes', function(oldValue, newValue) {
        if (newValue.length !== oldValue.length) {
          outcomesChanged();
        }
      });
    });

    InterventionResource.query(projectIdParam).$promise.then(function(interventions) {
      // add intervention details to interventionInclusions
      $scope.analysis.interventionInclusions = $scope.analysis.interventionInclusions.map(function(selectedIntervention) {
        return _.find(interventions, function(intervention) {
          return selectedIntervention.interventionId === intervention.id;
        });
      });
      // use same object in options list as in selected option list, as ui-select uses object equality internaly
      $scope.interventions = SingleStudyBenefitRiskService.concatWithNoDuplicates(interventions, $scope.analysis.interventionInclusions, isIdEqual);
      deregisterInterventionWatch = $scope.$watchCollection('analysis.interventionInclusions', function(oldValue, newValue) {
        if (newValue.length !== oldValue.length) {
          interventionsChanged();
        }
      });
    });

    ProjectStudiesResource.query({
      projectId: currentProject.id
    }).$promise.then(function(studies) {
      $scope.studies = studies;
      $scope.studyArrayLength = studies.length;

      $scope.studyModel.selectedStudy = _.find(studies, function(study) {
        return study.studyUri === $scope.analysis.studyGraphUri;
      });

      $scope.studies = SingleStudyBenefitRiskService.addMissingOutcomesToStudies($scope.studies, $scope.analysis.selectedOutcomes);
      $scope.studies = SingleStudyBenefitRiskService.addMissingInterventionsToStudies($scope.studies, $scope.analysis.interventionInclusions);
      SingleStudyBenefitRiskService.addHasMatchedMixedTreatmentArm($scope.studies, $scope.analysis.interventionInclusions);
      $scope.studies = SingleStudyBenefitRiskService.addOverlappingInterventionsToStudies($scope.studies, $scope.analysis.interventionInclusions);
      SingleStudyBenefitRiskService.recalculateGroup($scope.studies);
    });

    function analysisToSaveCommand(analysis) {
      var saveCommand = angular.copy(analysis);
      saveCommand.interventionInclusions = saveCommand.interventionInclusions.map(function(intervention) {
        return {
          interventionId: intervention.id,
          analysisId: saveCommand.id
        };
      });
      return saveCommand;
    }

    function saveAnalysis() {
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        // necessary because angular-select uses $watchcollection instead of $watch
        $scope.studies = $scope.studies.splice(0, $scope.studyArrayLength);
      });
    }

    $scope.onStudySelect = function(item) {
      $scope.analysis.studyGraphUri = item.studyUri;
      saveAnalysis();
    };

    $scope.$watch('studyModel.selectedStudy.overlappingInterventions', function(newValue) {
      $scope.overlappingInterventionsList = _.uniq(_.map(newValue, 'name')).join(', ');
    });

    $scope.goToDefaultScenarioView = function() {
      SingleStudyBenefitRiskService
        .getDefaultScenario()
        .then(function(scenario) {
          $state.go(DEFAULT_VIEW, _.extend($stateParams, {
            id: scenario.id
          }));
        });
    };

    $scope.createProblem = function() {
      if (deregisterOutcomeWatch) {
        deregisterOutcomeWatch();
      }
      if (deregisterInterventionWatch) {
        deregisterInterventionWatch();
      }
      SingleStudyBenefitRiskService.getProblem($scope.analysis).then(function(problem) {
        $scope.analysis.problem = problem;
        var saveCommand = analysisToSaveCommand($scope.analysis);
        AnalysisResource.save(saveCommand).$promise.then(function(response) {
          $scope.analysis = response;
          $scope.workspace = response;
          $scope.goToDefaultScenarioView();
        });
      });
    };

  };
  return dependencies.concat(SingleStudyBenefitRiskController);
});
