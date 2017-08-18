'use strict';

define(function(require) {
  var angular = require('angular');

  return angular.module('addis.project', ['ngResource'])
    // resources
    .factory('ReportResource', require('project/report/reportResource'))
    .factory('AnalysisResource', require('resources/analysisResource'))
    .factory('ProblemResource', require('bower_components/gemtc-web/app/js/analyses/problemResource'))

  // controllers
  .controller('CreateProjectModalController', require('project/createProjectModalController'))
    .controller('EditProjectController', require('project/editProjectController'))
    .controller('EditReportController', require('project/report/editReportController'))
    .controller('DeleteDefinitionController', require('project/deleteDefinitionController'))
    .controller('UpdateProjectController', require('project/updateProjectController'))
    .controller('CopyProjectController', require('project/copyProjectController'))
    .controller('InsertDirectiveController', require('project/report/insertDirectiveController'))
    .controller('RepairInterventionController', require('project/repairInterventionController'))
    .controller('AddScaledUnitController', require('project/addScaledUnitController'))

  //directives
  .directive('nmaReportView', require('project/nmaReportViewDirective'))
    .directive('ssbrReportView', require('project/ssbrReportViewDirective'))
    .directive('markdownReport', require('project/report/markdownReportDirective'))
    .directive('comparisonResult', require('project/report/comparisonResult/comparisonResultDirective'))
    .directive('relativeEffectsTable', require('project/report/relativeEffectsTable/relativeEffectsTableDirective'))
    .directive('relativeEffectsPlot', require('project/report/relativeEffectsPlot/relativeEffectsPlotDirective'))
    .directive('rankProbabilitiesTable', require('project/report/rankProbabilitiesTable/rankProbabilitiesTableDirective'))
    .directive('rankProbabilitiesPlot', require('project/report/rankProbabilitiesPlot/rankProbabilitiesPlotDirective'))
    .directive('forestPlot', require('project/report/forestPlot/forestPlotDirective'))
    .directive('treatmentEffects', require('project/report/treatmentEffects/treatmentEffectsDirective'))

  //services
  .service('ProjectService', require('project/projectService'))
    .service('ReportDirectiveService', require('project/report/reportDirectiveService'))
    .service('DefaultReportService', require('project/report/defaultReportService'))
    .service('CacheService', require('project/report/cacheService'));
});
