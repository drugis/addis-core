define(['angular', 'angular-mocks', 'jQuery', 'app'], function () {
  describe("App config", function () {

    var $rootScope, $state, $injector, myServiceMock, $location;

    beforeEach(module('addis'));
    beforeEach(inject( function( _$rootScope_, _$state_, _$injector_, _$location_, $urlRouter, $templateCache) {
      $rootScope = _$rootScope_;
      $state = _$state_;
      $injector = _$injector_;
      $location = _$location_;

      // We need add the template entry into the templateCache if we ever
      // specify a templateUrl
      $templateCache.put('projects.html', '');
    }));

    it('navigate to #/projects from projects', function() {
      expect($state.href('projects')).toEqual('#/projects');
    });

    xit('should navigate to #/projects by default', function() {
      $location.url('test');
      expect($location.path()).toEqual('#/projects');
    });

    it('should navigate to #/projects/1 ', function() {
      expect($state.href('project', { id: 1 })).toEqual('#/projects/1');
    });

  });
});