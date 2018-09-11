'use strict';
define(['angular-mocks', './user'], function() {
  describe('the user  service', function() {

    var userService, rootScope;
    var mockWindow = {
      sessionStorage: jasmine.createSpyObj('sessionStorage', ['getItem', 'setItem', 'removeItem'])
    };
    var userResourceMock = jasmine.createSpyObj('UserResource', ['get']);

    beforeEach(angular.mock.module('trialverse.user', function($provide) {
      $provide.value('$window', mockWindow);
      $provide.value('UserResource', userResourceMock);
    }));

    beforeEach(inject(function($rootScope, UserService) {
      rootScope = $rootScope;
      userService = UserService;
    }));

    describe('getLoginUser', function() {
      describe('if a user is cached', function() {
        var cachedUser = {id: 'cachedUser'};
        beforeEach(function() {
          mockWindow.sessionStorage.getItem.and.returnValue(JSON.stringify(cachedUser));
          mockWindow.sessionStorage.getItem.calls.reset();
          userResourceMock.get.calls.reset();
        });
        it('should return the user', function(done) {
          var userPromise = userService.getLoginUser();
          userPromise.then(function(user) {
            expect(user).toEqual(cachedUser);
            expect(mockWindow.sessionStorage.getItem).toHaveBeenCalled();
            expect(userResourceMock.get).not.toHaveBeenCalled();
            done();
          });
          rootScope.$apply();
        });
      });

      // it('should retrieve and return the user if one is logged in but not cached', function() {
      //   mockWindow.configs = {};
      //   expect(userService.hasLoggedInUser()).toBe(false);
      // });
    });

  });
});
