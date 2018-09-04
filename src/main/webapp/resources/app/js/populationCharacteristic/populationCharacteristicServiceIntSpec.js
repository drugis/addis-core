'use strict';
define(['angular-mocks', './populationCharacteristic'], function() {
  describe('the population characteristic service', function() {

    var rootScope, q,
      populationCharacteristicService,
      outcomeServiceMock = jasmine.createSpyObj('OutcomeService', ['queryItems', 'addItem', 'editItem', 'deleteItem']),
      outcomeQueryDefer,
      outcomeAddDefer,
      outcomeEditDefer,
      outcomeDeleteDefer;

    beforeEach(function() {
      angular.mock.module('trialverse.populationCharacteristic', function($provide) {
        $provide.value('OutcomeService', outcomeServiceMock);
      });
    });
    beforeEach(angular.mock.module('trialverse.populationCharacteristic'));

    beforeEach(inject(function($q, $rootScope, PopulationCharacteristicService) {
      q = $q;
      rootScope = $rootScope;
      populationCharacteristicService = PopulationCharacteristicService;

      outcomeQueryDefer = q.defer();
      outcomeServiceMock.queryItems.and.returnValue(outcomeQueryDefer.promise);
      outcomeAddDefer = q.defer();
      outcomeServiceMock.addItem.and.returnValue(outcomeAddDefer.promise);
      outcomeEditDefer = q.defer();
      outcomeServiceMock.editItem.and.returnValue(outcomeEditDefer.promise);
      outcomeDeleteDefer = q.defer();
      outcomeServiceMock.deleteItem.and.returnValue(outcomeDeleteDefer.promise);
    }));

    describe('query population characteristics', function() {
      beforeEach(function() {
        outcomeQueryDefer.resolve([{
          id: 'item1'
        }]);
      });

      it('should query the events', function(done) {
        populationCharacteristicService.queryItems().then(function(items) {
          expect(items.length).toBe(1);
          expect(outcomeServiceMock.queryItems).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('add population characteristic', function() {
      beforeEach(function(){
        outcomeAddDefer.resolve({});
      });
      it('should add the population characteristic', function(done) {
        populationCharacteristicService.addItem({}).then(function() {
          expect(outcomeServiceMock.addItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('edit population characteristic', function() {
      beforeEach(function(){
        outcomeEditDefer.resolve({});
      });
      it('should edit the population characteristic', function(done) {
        populationCharacteristicService.editItem({}).then(function() {
          expect(outcomeServiceMock.editItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });
    describe('delete population characteristic', function() {
      beforeEach(function(){
        outcomeDeleteDefer.resolve({});
      });
      it('should delete the population characteristic', function(done) {
        populationCharacteristicService.deleteItem({}).then(function() {
          expect(outcomeServiceMock.deleteItem).toHaveBeenCalled();
          done();
        });
        rootScope.$digest();
      });
    });

  });
});
