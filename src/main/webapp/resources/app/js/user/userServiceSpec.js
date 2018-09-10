'use strict';
define(['angular-mocks', './user'], function() {
  describe('the user  service', function() {

    var userService;
    var mockWindow;

    beforeEach(angular.mock.module('trialverse.user', function($provide){
      $provide.value('$window', mockWindow);
      $provide.value('UserResource', userResourceMock);
    }));


    beforeEach(inject(function($window, UserService) {
      mockWindow = $window;
      mockWindow.sessionStorage = {};
      userService = UserService;
    }));


    describe('getLoginUser', function() {
      it('should return the user if one is logged in and cached', function(done) {
        mockWindow.sessionStorage.getItem = function() {
          return 'cachedUser';
        };
        var userPromise = userService.getLoginUser();
        userPromise.then(function(user) {
          expect(user).toEqual('cachedUser');
          done();
        });
      });
      it('should retrieve and return the user if one is logged in but not cached', function() {
        mockWindow.configs = {};
        expect(userService.hasLoggedInUser()).toBe(false);
      });
    });

  });
});
