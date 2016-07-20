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


    describe('hasLogedInUser', function() {
      it('should return false if no user in LogedIn', function() {
        mockWindow.config.user = {
          id: 1
        };
        expect(userService.hasLogedInUser()).toBe(true);
      });
      it('should return false if no user in LogedIn', function() {
        mockWindow.configs = {};
        expect(userService.hasLogedInUser()).toBe(false);
      });
    });

  });
});
