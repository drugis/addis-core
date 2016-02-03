'use strict';
/* global describe, beforeEach, inject, it, expect */

define(['angular', 'angular-mocks', 'jQuery', 'app'], function() {
  describe('App config', function() {

    var $rootScope, $state, $injector, $location, $httpBackend;

    beforeEach(module('trialverse'));
    beforeEach(inject(function(_$rootScope_, _$state_, _$injector_, _$location_, _$httpBackend_, $urlRouter, $templateCache) {
      $rootScope = _$rootScope_;
      $state = _$state_;
      $injector = _$injector_;
      $location = _$location_;
      $httpBackend = _$httpBackend_;

      // We need add the template entry into the templateCache if we ever
      // specify a templateUrl
      $templateCache.put('user.html', '');
    }));

    it('should navigate to /users/ by default', function() {
      $location.url('test');
      $httpBackend.expect('GET', 'app/js/user/user.html')
        .respond(200);

      $rootScope.$apply();
      expect($location.path()).toEqual('/users/');
    });
  });
});