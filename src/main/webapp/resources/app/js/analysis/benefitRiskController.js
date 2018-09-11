'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$scope',
    '$q',
    '$stateParams',
    '$state',
    'AnalysisResource',
    'BenefitRiskService', 
    'InterventionResource',
    'ModelResource', 
    'OutcomeResource', 
    'PageTitleService',
    'ProjectResource',  
    'ProjectStudiesResource', 
    'ScenarioResource', 
    'SubProblemResource', 
    'TrialverseResource', 
    'UserService', 
    'DEFAULT_VIEW'
  ];
  var BenefitRiskController = function(
    $scope,
    $q,
    $stateParams,
    $state,
    AnalysisResource,
    BenefitRiskService, 
    InterventionResource,
    ModelResource, 
    OutcomeResource, 
    PageTitleService,
    ProjectResource,
    ProjectStudiesResource, 
    ScenarioResource, 
    SubProblemResource, 
    TrialverseResource, 
    UserService, 
    DEFAULT_VIEW
  ) {
    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.models = ModelResource.getConsistencyModels($stateParams);
    $scope.goToDefaultScenario = goToDefaultScenario;
    $scope.goToModel = goToModel;
    $scope.userId = $stateParams.userUid;
    $scope.project = ProjectResource.get($stateParams);
    var studiesPromise = ProjectStudiesResource.query({
      projectId: $stateParams.projectId
    }).$promise;

    $scope.editMode = {
      allowEditing: false
    };

    var promises = [$scope.analysis.$promise,
    $scope.alternatives.$promise,
    $scope.outcomes.$promise,
    $scope.models.$promise,
    $scope.project.$promise,
      studiesPromise
    ];

    $scope.loadingPromise = $q.all(promises).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];
      var outcomes = result[2];
      var models = result[3];
      var project = result[4];
      var studies = _.map(result[5], function(study) {
        return _.extend({}, study, {
          uuid: study.studyUri.split('/graphs/')[1]
        });
      });

      PageTitleService.setPageTitle('BenefitRiskController', analysis.title);

      if (UserService.isLoginUserId(project.owner.id) && !analysis.archived) {
        $scope.editMode.allowEditing = true;
      }

      $scope.projectVersionUuid = project.datasetVersion.split('/versions/')[1];
      TrialverseResource.get({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      }).$promise.then(function(dataset) {
        $scope.datasetOwnerId = dataset.ownerId;
      });

      var outcomeIds = _.map(outcomes, 'id');

      AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise.then(function(networkMetaAnalyses) {
        networkMetaAnalyses = networkMetaAnalyses
          .map(_.partial(BenefitRiskService.joinModelsWithAnalysis, models))
          .map(BenefitRiskService.addModelsGroup);
        var outcomesWithAnalyses = BenefitRiskService.buildOutcomesWithAnalyses(analysis, outcomes, networkMetaAnalyses);
        outcomesWithAnalyses = BenefitRiskService.addStudiesToOutcomes(
          outcomesWithAnalyses, analysis.benefitRiskStudyOutcomeInclusions, studies);
        outcomesWithAnalyses = _.partition(outcomesWithAnalyses, ['dataType', 'network']);
        $scope.networkOWAs = outcomesWithAnalyses[0];
        $scope.studyOutcomes = outcomesWithAnalyses[1];
        $scope.isMissingBaseline = _.find($scope.networkOWAs, function(outcomeWithAnalysis) {
          return !outcomeWithAnalysis.baselineDistribution;
        });
      });

      $scope.alternatives = alternatives.map(function(alternative) {
        var isAlternativeInInclusions = _.find(analysis.interventionInclusions, function(includedAlternative) {
          return includedAlternative.id === alternative.id;
        });
        if (isAlternativeInInclusions) {
          alternative.isIncluded = true;
        }
        return alternative;
      });
      setIncludedAlternatives();

      $scope.outcomes = outcomes.map(function(outcome) {
        var isOutcomeInInclusions = _.find(analysis.benefitRiskNMAOutcomeInclusions, function(benefitRiskNMAOutcomeInclusion) {
          return benefitRiskNMAOutcomeInclusion.outcomeId === outcome.id;
        });
        if (isOutcomeInInclusions) {
          outcome.isIncluded = true;
        }
        return outcome;
      });
    });

    function setIncludedAlternatives() {
      $scope.analysis.interventionInclusions = $scope.alternatives.filter(function(alternative) {
        return alternative.isIncluded;
      });
    }

    function goToDefaultScenario() {
      var params = $stateParams;
      SubProblemResource.query(params).$promise.then(function(subProblems) {
        var subProblem = subProblems[0];
        params = _.extend({}, params, {
          problemId: subProblem.id
        });
        ScenarioResource.query(params).$promise.then(function(scenarios) {
          $state.go(DEFAULT_VIEW, {
            userUid: $scope.userId,
            projectId: params.projectId,
            analysisId: params.analysisId,
            problemId: subProblem.id,
            id: scenarios[0].id
          });
        });
      });
    }

    function goToModel(model) {
      $state.go('model', {
        userUid: $scope.userId,
        projectId: $stateParams.projectId,
        analysisId: model.analysisId,
        modelId: model.id
      });
    }

  };
  return dependencies.concat(BenefitRiskController);
});
