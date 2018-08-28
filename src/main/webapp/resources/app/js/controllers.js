'use strict';
define(
  [
    'angular',
    './controllers/projectsController',
    './controllers/singleProjectController',
    './controllers/networkMetaAnalysisContainerController',
    './controllers/networkMetaAnalysisModelContainerController'
  ],
  function(
    angular,
    ProjectsController,
    SingleProjectController,
    NetworkMetaAnalysisContainerController,
    NetworkMetaAnalysisModelContainerController
  ) {
    return angular.module('addis.controllers', [])
      .controller('ProjectsController', ProjectsController)
      .controller('SingleProjectController', SingleProjectController)
      .controller('NetworkMetaAnalysisContainerController', NetworkMetaAnalysisContainerController)
      .controller('NetworkMetaAnalysisModelContainerController', NetworkMetaAnalysisModelContainerController);
  });
