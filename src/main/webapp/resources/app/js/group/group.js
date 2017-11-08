'use strict';
var requires = [
  'group/editGroupController',
  'group/createGroupController',
  'group/groupService'
];
define(requires.concat(['angular', 'angular-resource']), function(
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
});