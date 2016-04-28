'use strict';
define(['lodash'], function(_) {
  var dependencies = ['EvidenceTableResource', 'NetworkMetaAnalysisService',
    'ModelResource', 'AnalysisService', 'PataviTaskIdResource', 'PataviService',
    'ProblemResource'
  ];
  var nmaReportViewDirective = function(EvidenceTableResource,
    NetworkMetaAnalysisService, ModelResource, AnalysisService,
    PataviTaskIdResource, PataviService, ProblemResource) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/project/nmaReportView.html',
      scope: {
        user: '=',
        project: '=',
        analysis: '=',
        interventions: '='
      },
      link: function(scope) {
        EvidenceTableResource.query({
            projectId: scope.project.id,
            analysisId: scope.analysis.id
          })
          .$promise.then(function(trialverseData) {
            scope.interventions = NetworkMetaAnalysisService.addInclusionsToInterventions(scope.interventions, scope.analysis.interventionInclusions);
            var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions(scope.interventions);
            scope.network = NetworkMetaAnalysisService.transformTrialDataToNetwork(trialverseData, includedInterventions, scope.analysis);
          });


        var modelPromise = ModelResource.query({
          projectId: scope.project.id,
          analysisId: scope.analysis.id
        }).$promise;

        scope.problem = ProblemResource.get({
          analysisId: scope.analysis.id,
          projectId: scope.project.id
        });

        modelPromise.then(function(models) {
          scope.primaryModel = _.find(models, function(model) {
            return model.id === scope.analysis.primaryModel;
          });

          scope.otherModels = _.filter(models, function(model) {
            return model.id !== scope.analysis.primaryModel;
          });

          getResults(scope.primaryModel);
        });

        scope.modelSettingSummary = function(model) {
          if (!model) {
            return '';
          }
          var modelPart = model.linearModel === 'fixed' ? 'fixed effect' : 'random effects';
          var typePart = modelTypeString(model.modelType.type);
          var scalePart = AnalysisService.getScaleName(model);
          return modelPart + ' ' + typePart + ' on the ' + scalePart + ' scale.';
        };

        var getResults = function(model) {
          PataviTaskIdResource
            .get({projectId: scope.project.id, analysisId: scope.analysis.id, modelId: model.id})
            .$promise
            .then(PataviService.run)
            .then(function(result) {
              scope.result = result.results;
            },
            function(pataviError) {
              console.error('an error has occurred, error: ' + JSON.stringify(pataviError));
              scope.$emit('error', {
                type: 'patavi',
                message: pataviError.desc
              });
            });

      };

      function modelTypeString(type) {
        var typeString;
        if (type === 'network') {
          typeString = 'network meta-analysis';
        } else if (type === 'pair-wise') {
          typeString = 'pair-wise meta-analysis';
        } else if (type === 'node-split') {
          typeString = 'node-splitting analysis';
        } else if (type === 'regression') {
          typeString = 'meta-regression';
        }
        return typeString;
      }


    }
  };
};
return dependencies.concat(nmaReportViewDirective);
});
