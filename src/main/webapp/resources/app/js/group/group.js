'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.group', ['ngResource', 
    'trialverse.util', 
    'trialverse.study'])
    // controllers
    .controller('EditGroupController', require('group/editGroupController'))
    .controller('CreateGroupController', require('group/createGroupController'))

    //services
    .factory('GroupService', require('group/groupService'))
    ;
});
