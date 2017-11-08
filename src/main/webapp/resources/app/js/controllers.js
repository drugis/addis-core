'use strict';
var requires = [
  'controllers/projectsController',
  'controllers/singleProjectController',
  'controllers/networkMetaAnalysisContainerController',
  'controllers/networkMetaAnalysisModelContainerController'
];
define(['angular'].concat(requires), function(
	angular,
  ProjectsController,
  SingleProjectController,
  NetworkMetaAnalysisContainerController,
  NetworkMetaAnalysisModelContainerController) {
  return angular.module('addis.controllers', [])
    .controller('ProjectsController', ProjectsController)
    .controller('SingleProjectController', SingleProjectController)
    .controller('NetworkMetaAnalysisContainerController', NetworkMetaAnalysisContainerController)
    .controller('NetworkMetaAnalysisModelContainerController', NetworkMetaAnalysisModelContainerController);
});