'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the adverse event service', function() {

    var rootScope, q,
      adverseEventService,
      outcomeServiceMock = jasmine.createSpyObj('OutcomeService', ['queryItems', 'addItem', 'editItem', 'deleteItem']),
      outcomeQueryDefer,
      outcomeAddDefer,
      outcomeEditDefer,
      outcomeDeleteDefer;

    beforeEach(function() {
      module('trialverse.adverseEvent', function($provide) {
        $provide.value('OutcomeService', outcomeServiceMock);
      });
    });
    beforeEach(module('trialverse.adverseEvent'));

    beforeEach(angularMocks.inject(function($q, $rootScope, AdverseEventService) {
      q = $q;
      rootScope = $rootScope;
      adverseEventService = AdverseEventService;

      outcomeQueryDefer = q.defer();
      outcomeServiceMock.queryItems.and.returnValue(outcomeQueryDefer.promise);
      outcomeAddDefer = q.defer();
      outcomeServiceMock.addItem.and.returnValue(outcomeAddDefer.promise);
      outcomeEditDefer = q.defer();
      outcomeServiceMock.editItem.and.returnValue(outcomeEditDefer.promise);
      outcomeDeleteDefer = q.defer();
      outcomeServiceMock.deleteItem.and.returnValue(outcomeDeleteDefer.promise);
    }));

    describe('query adverse events', function() {
      beforeEach(function() {
        outcomeQueryDefer.resolve([{
          id: 'item1'
        }]);
      });

      it('should query the events', function(done) {
        adverseEventService.queryItems().then(function(items) {
          expect(items.length).toBe(1);
          expect(outcomeServiceMock.queryItems).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('add adverse event', function() {
      beforeEach(function(){
        outcomeAddDefer.resolve({});
      });
      it('should add the advere event', function(done) {
        adverseEventService.addItem({}).then(function() {
          expect(outcomeServiceMock.addItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('edit adverse event', function() {
      beforeEach(function(){
        outcomeEditDefer.resolve({});
      });
      it('should edit the adverse event', function(done) {
        adverseEventService.editItem({}).then(function() {
          expect(outcomeServiceMock.editItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('delete adverse event', function() {
      beforeEach(function(){
        outcomeDeleteDefer.resolve({});
      });
      it('should delete the adverse event', function(done) {
        adverseEventService.deleteItem({}).then(function() {
          expect(outcomeServiceMock.deleteItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });

  });
});
