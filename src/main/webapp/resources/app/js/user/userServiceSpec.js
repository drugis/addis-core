'use strict';
define(['angular-mocks', './user'], function() {
  describe('the user service', function() {
    var userService, rootScope;
    var mockWindow = {
      sessionStorage: jasmine.createSpyObj('sessionStorage', ['setItem', 'removeItem'])
    };
    var userResourceMock = jasmine.createSpyObj('UserResource', ['get']);
    var userDefer;
    var henkDeVries = {
      id: 'userId',
      firstName: 'Henk',
      lastName: 'de Vries',
      email: 'henk@deVries.nl'
    };

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
      beforeEach(function() {
        userResourceMock.get.calls.reset();
        mockWindow.sessionStorage.setItem.calls.reset();
      });

      it('should retrieve and return the user if one is logged in', function(done) {
        var userPromise = userService.getLoginUser();
        userPromise.then(function(user) {
          expect(user).toEqual(henkDeVries);
          done();
        });
        userDefer.resolve({
          id: 'userId',
          firstName: 'Henk',
          lastName: 'de Vries',
          this: 'will be stripped',
          email: 'henk@deVries.nl'
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

    describe('isLoginUserId', function() {
      beforeEach(function() {
        userResourceMock.get.calls.reset();
        mockWindow.sessionStorage.setItem.calls.reset();
      });

      it('should return a promise whoms result yields true if the entered id is the id of the logged in user', function(done) {
        userService.isLoginUserId(henkDeVries.id).then(function(result) {
          expect(result).toBeTruthy();
          done();
        });
        userDefer.resolve(henkDeVries);
        rootScope.$apply();
      });

      it('should return a promise whoms result yields false if the entered id is not the id of the logged in user', function(done) {
        userService.isLoginUserId('not Henk').then(function(result) {
          expect(result).toBeFalsy();
          done();
        });
        userDefer.resolve(undefined);
        rootScope.$apply();
      });
    });

    describe('isLoginUserEmail', function() {
      beforeEach(function() {
        userResourceMock.get.calls.reset();
      });

      it('should return a promise whoms result yields true if the entered email is the email of the logged in user', function(done) {
        userService.isLoginUserEmail(henkDeVries.email).then(function(result) {
          expect(result).toBeTruthy();
          done();
        });
        userDefer.resolve(henkDeVries);
        rootScope.$apply();
      });

      it('should return a promise whoms result yields false if the entered email is not the email of the logged in user', function(done) {
        userService.isLoginUserEmail('some@thing.else').then(function(result) {
          expect(result).toBeFalsy();
          done();
        });
        userDefer.resolve(henkDeVries);
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
