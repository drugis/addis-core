'use strict';
var requires = [
  'home/homeController',
  'user/userResource'
];
define(requires.concat(['angular', 'angular-resource']), function(
  HomeController,
  UserResource,
  angular) {
  return angular.module('addis.home', ['ngResource', 'trialverse.util'])
    // controllers
    .controller('HomeController', HomeController)

    //resources
    .factory('UserResource', UserResource);
});