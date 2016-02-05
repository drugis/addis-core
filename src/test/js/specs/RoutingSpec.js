'use strict';
define(['angular', 'angular-mocks', 'jQuery', 'app'], function () {
  describe("App config", function () {
    var $rootScope, $state, $injector, $location, $httpBackend;
    beforeEach(module('addis'));
    beforeEach(inject(function (_$rootScope_, _$state_, _$injector_, _$location_, _$httpBackend_, $templateCache) {
      $rootScope = _$rootScope_;
      $state = _$state_;
      $injector = _$injector_;
      $location = _$location_;
      $httpBackend = _$httpBackend_;
      // We need add the template entry into the templateCache if we ever
      // specify a templateUrl
      $templateCache.put('projects.html', '');
      $httpBackend.expect('GET', 'app/js/bower_components/gemtc-web/app/lexicon.json').respond(200);
    }));
    it('navigate to #/projects from projects', function () {
      expect($state.href('projects', {userUid: 1})).toEqual('#/users/1/projects');
    });

    it('should navigate to #/projects/1 ', function () {
      expect($state.href('project', {
        userUid: 37,
        projectId: 1
      })).toEqual('#/users/37/projects/1');
    });
    it('should navigate to #/projects/1/nma/2 ', function () {
      expect($state.href('networkMetaAnalysis', {
        userUid: 42,
        projectId: 1,
        analysisId: 2
      })).toEqual('#/users/42/projects/1/nma/2');
    });
    it('should navigate to #/projects/1/ssbr/2 ', function () {
      expect($state.href('singleStudyBenefitRisk', {
        userUid: 1,
        projectId: 2,
        analysisId: 3
      })).toEqual('#/users/1/projects/2/ssbr/3');
    });
  });
});
