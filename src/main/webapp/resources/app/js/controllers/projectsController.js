'use strict';
define([], function() {
  var dependencies = ['$scope', '$window', '$location', 'ProjectResource', 'TrialverseResource'];
  var ProjectsController = function($scope, $window, $location, ProjectResource, TrialverseResource) {
    $scope.user = $window.config.user;
    $scope.projects = ProjectResource.query();
    $scope.trialverse = TrialverseResource.query();

    $scope.createProject = function (newProject) {
      // clear modal form by resetting model in current scope
      this.model = {};
      ProjectResource.save(newProject, function() {
        $scope.projects = ProjectResource.query(function(){
          $scope.createProjectModal.close();
        });
      });
    };

    var ws = new WebSocket('ws://localhost:8080/handler');

    ws2 = new WebSocket('ws://localhost:3000/ws');

    $scope.sendMessage2 = function() {
      ws2.send('test yo');
    };   

    ws.onopen = function(event) {
      console.log('open' + event);
    };

    ws.onmessage = function(event) {
      console.log('message' + event);
    };

    $scope.sendMessage = function() {
      ws.send('test yo');
    };   
  };
  return dependencies.concat(ProjectsController);
});
