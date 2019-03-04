'use strict';
define([
  './editGroupController',
  './createGroupController',
  './groupService',
  'angular', 'angular-resource'
],
  function(
    EditGroupController,
    CreateGroupController,
    GroupService,
    angular
  ) {
    return angular.module('trialverse.group', ['ngResource',
      'trialverse.util',
      'trialverse.study'
    ])
      // controllers
      .controller('EditGroupController', EditGroupController)
      .controller('CreateGroupController', CreateGroupController)

      //services
      .factory('GroupService', GroupService);
  }
);
