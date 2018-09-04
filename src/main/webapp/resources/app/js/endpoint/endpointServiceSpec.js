'use strict';
define(['angular-mocks', './endpoint'], function() {
  describe('the endpoint service', function() {

    var rootScope, q,
      endpointService,
      outcomeServiceMock = jasmine.createSpyObj('OutcomeService', ['queryItems', 'addItem', 'editItem', 'deleteItem']),
      outcomeQueryDefer,
      outcomeAddDefer,
      outcomeEditDefer,
      outcomeDeleteDefer;

    beforeEach(function() {
      angular.mock.module('trialverse.endpoint', function($provide) {
        $provide.value('OutcomeService', outcomeServiceMock);
      });
    });
    beforeEach(angular.mock.module('trialverse.endpoint'));

    beforeEach(inject(function($q, $rootScope, EndpointService) {
      q = $q;
      rootScope = $rootScope;
      endpointService = EndpointService;

      outcomeQueryDefer = q.defer();
      outcomeServiceMock.queryItems.and.returnValue(outcomeQueryDefer.promise);
      outcomeAddDefer = q.defer();
      outcomeServiceMock.addItem.and.returnValue(outcomeAddDefer.promise);
      outcomeEditDefer = q.defer();
      outcomeServiceMock.editItem.and.returnValue(outcomeEditDefer.promise);
      outcomeDeleteDefer = q.defer();
      outcomeServiceMock.deleteItem.and.returnValue(outcomeDeleteDefer.promise);
    }));

    describe('query endpoints', function() {
      beforeEach(function() {
        outcomeQueryDefer.resolve([{
          id: 'item1'
        }]);
      });

      it('should query the events', function(done) {
        endpointService.queryItems().then(function(items) {
          expect(items.length).toBe(1);
          expect(outcomeServiceMock.queryItems).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('add endpoint', function() {
      beforeEach(function(){
        outcomeAddDefer.resolve({});
      });
      it('should add the endpoint', function(done) {
        endpointService.addItem({}).then(function() {
          expect(outcomeServiceMock.addItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('edit endpoint', function() {
      beforeEach(function(){
        outcomeEditDefer.resolve({});
      });
      it('should edit the endpoint', function(done) {
        endpointService.editItem({}).then(function() {
          expect(outcomeServiceMock.editItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('delete endpoint', function() {
      beforeEach(function(){
        outcomeDeleteDefer.resolve({});
      });
      it('should delete the endpoint', function(done) {
        endpointService.deleteItem({}).then(function() {
          expect(outcomeServiceMock.deleteItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });

  });
});
