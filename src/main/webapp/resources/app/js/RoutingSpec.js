define(['angular', 'angular-mocks', 'jQuery', 'app'], function () {
  describe("App config", function () {

    var $rootScope, $state, $injector, $location, $httpBackend;

    beforeEach(module('trialverse'));
    beforeEach(inject(function (_$rootScope_, _$state_, _$injector_, _$location_, _$httpBackend_, $urlRouter, $templateCache) {
      $rootScope = _$rootScope_;
      $state = _$state_;
      $injector = _$injector_;
      $location = _$location_;
      $httpBackend = _$httpBackend_;

      // We need add the template entry into the templateCache if we ever
      // specify a templateUrl
      $templateCache.put('datasets.html', '');
    }));

    it('should navigate to /datasets by default', function () {
      $location.url('test');
      $httpBackend.expect('GET', 'app/js/dataset/datasets.html')
        .respond(200);

      $rootScope.$apply();
      expect($location.path()).toEqual('/datasets');
    });

  });
});
