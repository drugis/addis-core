'use strict';
var requires = ['excelExport/excelExportService'];
define(requires.concat(['angular', 'angular-resource']), function(
  ExcelExportService,
  angular
) {
  return angular.module('addis.excelExport', ['ngResource', 
  	'trialverse.study'])
    //services
    .factory('ExcelExportService', ExcelExportService);
});