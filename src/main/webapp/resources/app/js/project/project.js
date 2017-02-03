'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('addis.project', ['ngResource'])
    // resources
    .factory('ReportResource', require('project/reportResource'))

    // controllers
    .controller('CreateProjectModalController', require('project/createProjectModalController'))
    .controller('EditProjectController', require('project/editProjectController'))
    .controller('EditReportController', require('project/editReportController'))
    .controller('InsertNetworkGraphController', require('project/insertNetworkGraphController'))
    .controller('InsertComparisonResultController', require('project/insertComparisonResultController'))
    .controller('DeleteDefinitionController',require('project/deleteDefinitionController'))
    .controller('UpdateProjectController', require('project/updateProjectController'))
    .controller('CopyProjectController', require('project/copyProjectController'))

    //directives
    .directive('nmaReportView', require('project/nmaReportViewDirective'))
    .directive('ssbrReportView', require('project/ssbrReportViewDirective'))
    .directive('markdownReport', require('project/markdownReportDirective'))
    .directive('comparisonResult', require('project/comparisonResultDirective'))

    //services
    .service('ProjectService', require('project/projectService'))
    .service('ReportDirectiveService', require('project/reportDirectiveService'))
    ;
});
