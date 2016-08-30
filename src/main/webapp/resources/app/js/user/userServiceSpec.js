'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the user  service', function() {

    var userService;
    var mockWindow;

    beforeEach(module('trialverse.user'));

    beforeEach(angularMocks.inject(function($window, UserService) {
      mockWindow = $window;
      mockWindow.config = {};
      userService = UserService;
    }));


    describe('hasLoggedInUser', function() {
      it('should return false if no user in LoggedIn', function() {
        mockWindow.config.user = {
          id: 1
        };
        expect(userService.hasLoggedInUser()).toBe(true);
      });
      it('should return false if no user in LoggedIn', function() {
        mockWindow.configs = {};
        expect(userService.hasLoggedInUser()).toBe(false);
      });
    });

  });
});
