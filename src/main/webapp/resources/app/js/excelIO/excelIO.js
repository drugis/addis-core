'use strict';
define([
  './excelImportService',
  './excelExportService',
  './excelExportUtilService',
  './excelIOUtilService',
  'angular',
  'angular-resource',
  '../util/constants',
  '../study/study'],
  function(
    ExcelImportService,
    ExcelExportService,
    ExcelExportUtilService,
    ExcelIOUtilService,
    angular
  ) {
    return angular.module('addis.excelIO', [
      'ngResource',
      'addis.constants',
      'trialverse.study'
    ])
      //services
      .factory('ExcelImportService', ExcelImportService)
      .factory('ExcelExportService', ExcelExportService)
      .factory('ExcelIOUtilService', ExcelIOUtilService)
      .factory('ExcelExportUtilService', ExcelExportUtilService);
  }
);
