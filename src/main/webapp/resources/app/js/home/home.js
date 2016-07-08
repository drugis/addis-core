'use strict';

define(function(require) {
  var angular = require('angular');

  return angular.module('addis.home', ['ngResource', 'trialverse.util'])

  // controllers
  .controller('HomeController', require('home/homeController'))

  //resources
  .factory('UserResource', require('user/userResource'));
});
