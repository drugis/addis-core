'use strict';
define(['angular-mocks', './user'], function() {
  describe('the user service', function() {

    var userService, rootScope;
    var mockWindow = {
      sessionStorage: jasmine.createSpyObj('sessionStorage', ['getItem', 'setItem', 'removeItem'])
    };
    var userResourceMock = jasmine.createSpyObj('UserResource', ['get']);
    var userDefer;

    beforeEach(angular.mock.module('trialverse.user', function($provide) {
      $provide.value('$window', mockWindow);
      $provide.value('UserResource', userResourceMock);
    }));

    beforeEach(inject(function($rootScope, UserService, $q) {
      rootScope = $rootScope;
      userService = UserService;
      userDefer = $q.defer();
      userResourceMock.get.and.returnValue({
        $promise: userDefer.promise
      });
    }));

    describe('getLoginUser', function() {
      describe('if a user is cached', function() {
        var cachedUser = { id: 'cachedUser' };
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

      describe('if a user is not cached', function() {
        beforeEach(function() {
          userResourceMock.get.calls.reset();
          mockWindow.sessionStorage.getItem.and.returnValue(false);
          mockWindow.sessionStorage.getItem.calls.reset();
          mockWindow.sessionStorage.setItem.calls.reset();
        });
        it('should retrieve and return the user if one is logged in', function(done) {
          var userPromise = userService.getLoginUser();
          userPromise.then(function(user) {
            expect(user).toEqual({
              id: 'userId',
              firstName: 'Henk',
              lastName: 'de Vries'
            });
            done();
          });
          userDefer.resolve({
            id: 'userId',
            firstName: 'Henk',
            lastName: 'de Vries',
            this: 'will be stripped'
          });
          rootScope.$apply();
        });

        it('should return undefined if no user is logged in', function(done) {
          var userPromise = userService.getLoginUser();
          userPromise.then(function(user) {
            expect(user).toBe(undefined);
            done();
          });
          userDefer.resolve(undefined);
          rootScope.$apply();
        });
      });
    });

    describe('isLoginUserId', function() {
      var cachedUser = { id: 'cachedUser' };
      beforeEach(function() {
        mockWindow.sessionStorage.getItem.and.returnValue(JSON.stringify(cachedUser));
        mockWindow.sessionStorage.getItem.calls.reset();
        userResourceMock.get.calls.reset();
      });
      it('should return a promise whoms result yields true if the entered id is the id of the logged in user', function(done) {
        userService.isLoginUserId('cachedUser').then(function(result) {
          expect(result).toBeTruthy();
          done();
        });
        rootScope.$apply();
      });
      it('should return a promise whoms result yields flase if the entered id is not the id of the logged in user', function(done) {
        userService.isLoginUserId('notCachedUser').then(function(result) {
          expect(result).toBeFalsy();
          done();
        });
        rootScope.$apply();
      });
    });
    describe('isLoginUserEmail', function() {
      var cachedUser = {
        id: 'cachedUser',
        email: 'some@thing.com'
      };
      beforeEach(function() {
        mockWindow.sessionStorage.getItem.and.returnValue(JSON.stringify(cachedUser));
        mockWindow.sessionStorage.getItem.calls.reset();
        userResourceMock.get.calls.reset();
      });
      it('should return a promise whoms result yields true if the entered email is the email of the logged in user', function(done) {
        userService.isLoginUserEmail('some@thing.com').then(function(result) {
          expect(result).toBeTruthy();
          done();
        });
        rootScope.$apply();
      });
      it('should return a promise whoms result yields flase if the entered email is not the email of the logged in user', function(done) {
        userService.isLoginUserEmail('some@thing.else').then(function(result) {
          expect(result).toBeFalsy();
          done();
        });
        rootScope.$apply();
      });
    });
    describe('logOut', function() {
      it('should remove the user from the sessionStorage', function() {
        userService.logOut();
        expect(mockWindow.sessionStorage.removeItem).toHaveBeenCalledWith('user');
      });
    });
  });
});
