'use strict';
define(
  ['angular',
    'require',
    'jQuery',
    'foundation',
    'angular-ui-router',
    'controllers',
    'directives',
    'resources'],
  function (angular, require, $) {
    var dependencies = ['ui.router', 'addis.controllers', 'addis.directives', 'addis.resources'];
    var app = angular.module('addis', dependencies);

    app.run(['$rootScope', function ($rootScope) {

      $rootScope.$on('$viewContentLoaded', function () {
        $(document).foundation();
      });

    }]);

    app.config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
      var baseTemplatePath = "app/views/";

      // Default route
      $stateProvider.state('projects',
        { url: '/projects',
          templateUrl: baseTemplatePath + 'projects.html',
          controller: "ProjectsController" });
      $urlRouterProvider.otherwise('/projects');
    }]);


    return app;
});
