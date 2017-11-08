'use strict';
var requires = [
  'project/report/reportResource',
  'resources/analysisResource',
  'bower_components/gemtc-web/app/js/analyses/problemResource',
  'project/createProjectModalController',
  'project/editProjectController',
  'project/report/editReportController',
  'project/deleteDefinitionController',
  'project/updateProjectController',
  'project/copyProjectController',
  'project/report/insertDirectiveController',
  'project/repairInterventionController',
  'project/addScaledUnitController',
  'project/nmaReportViewDirective',
  'project/ssbrReportViewDirective',
  'project/report/markdownReportDirective',
  'project/report/comparisonResult/comparisonResultDirective',
  'project/report/relativeEffectsTable/relativeEffectsTableDirective',
  'project/report/relativeEffectsPlot/relativeEffectsPlotDirective',
  'project/report/rankProbabilitiesTable/rankProbabilitiesTableDirective',
  'project/report/rankProbabilitiesPlot/rankProbabilitiesPlotDirective',
  'project/report/forestPlot/forestPlotDirective',
  'project/report/treatmentEffects/treatmentEffectsDirective',
  'project/projectService',
  'project/report/reportDirectiveService',
  'project/report/defaultReportService',
  'project/report/cacheService'
];
define(requires.concat(['angular', 'angular-resource']), function(
  ReportResource,
  AnalysisResource,
  ProblemResource,
  CreateProjectModalController,
  EditProjectController,
  EditReportController,
  DeleteDefinitionController,
  UpdateProjectController,
  CopyProjectController,
  InsertDirectiveController,
  RepairInterventionController,
  AddScaledUnitController,
  nmaReportView,
  ssbrReportView,
  markdownReport,
  comparisonResult,
  relativeEffectsTable,
  relativeEffectsPlot,
  rankProbabilitiesTable,
  rankProbabilitiesPlot,
  forestPlot,
  treatmentEffects,
  ProjectService,
  ReportDirectiveService,
  DefaultReportService,
  CacheService,
  angular) {
  return angular.module('addis.project', ['ngResource'])
    // resources
    .factory('ReportResource', ReportResource)
    .factory('AnalysisResource', AnalysisResource)
    .factory('ProblemResource', ProblemResource)

    // controllers
    .controller('CreateProjectModalController', CreateProjectModalController)
    .controller('EditProjectController', EditProjectController)
    .controller('EditReportController', EditReportController)
    .controller('DeleteDefinitionController', DeleteDefinitionController)
    .controller('UpdateProjectController', UpdateProjectController)
    .controller('CopyProjectController', CopyProjectController)
    .controller('InsertDirectiveController', InsertDirectiveController)
    .controller('RepairInterventionController', RepairInterventionController)
    .controller('AddScaledUnitController', AddScaledUnitController)

    //directives
    .directive('nmaReportView', nmaReportView)
    .directive('ssbrReportView', ssbrReportView)
    .directive('markdownReport', markdownReport)
    .directive('comparisonResult', comparisonResult)
    .directive('relativeEffectsTable', relativeEffectsTable)
    .directive('relativeEffectsPlot', relativeEffectsPlot)
    .directive('rankProbabilitiesTable', rankProbabilitiesTable)
    .directive('rankProbabilitiesPlot', rankProbabilitiesPlot)
    .directive('forestPlot', forestPlot)
    .directive('treatmentEffects', treatmentEffects)

    //services
    .service('ProjectService', ProjectService)
    .service('ReportDirectiveService', ReportDirectiveService)
    .service('DefaultReportService', DefaultReportService)
    .service('CacheService', CacheService);
});