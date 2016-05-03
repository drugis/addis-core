'use strict';
define([], function() {
  var dependencies = ['ProblemResource', 'WorkspaceService', 'ScenarioResource', 'MCDAResultsService'];
  var SsbrReportViewDirective = function(ProblemResource, WorkspaceService, ScenarioResource, MCDAResultsService) {
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
          return problem;
        });

        scope.scenarios = ScenarioResource.query({
          analysisId: scope.analysis.id,
          projectId: scope.project.id
        }).$promise.then(function(scenarios) {
          scope.scenarios = scenarios.map(function(scenario) {
            scenario.state = MCDAResultsService.getResults(scope, scenario.state);
            return scenario;
          });
        });

      }
    };
  };
  return dependencies.concat(SsbrReportViewDirective);
});
