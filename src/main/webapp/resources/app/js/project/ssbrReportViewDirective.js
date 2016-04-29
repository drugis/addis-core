'use strict';
define(['lodash'], function(_) {
  var dependencies = ['AnalysisService', 'PataviTaskIdResource', 'PataviService', 'ProblemResource', 'WorkspaceService', 'EffectsTableService'];
  var SsbrReportViewDirective = function(AnalysisService, PataviTaskIdResource, PataviService, ProblemResource, WorkspaceService, EffectsTableService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/project/ssbrReportView.html',
      scope: {
        user: '=',
        project: '=',
        analysis: '=',
        interventions: '='
      },
      link: function(scope) {
        scope.problem = ProblemResource.get({
          analysisId: scope.analysis.id,
          projectId: scope.project.id
        }, function(problem) {
          problem.valueTree = WorkspaceService.buildValueTree(scope.problem);
          WorkspaceService.getObservedScales(scope.problem).then(function(scales) {
            scope.scales = scales;
          });
          scope.effectsTableData = EffectsTableService.buildEffectsTableData(problem, problem.valueTree);
          return problem;
        });


      }
    };
  };
  return dependencies.concat(SsbrReportViewDirective);
});
