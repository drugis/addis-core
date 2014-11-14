'use strict';
define(['angular', 'angular-mocks', 'rdfstore'],
  function(angulare, angularMocks, rdfstore) {
    describe('the rdfstore service', function() {

      var store;
      var mockStore = jasmine.createSpyObj('store', ['load', 'execute']);

      beforeEach(module('trialverse.rdfstoreWrap'));

      describe('load', function() {

        beforeEach(function() {
          store = mockStore;
        });

        describe('when not initialised', function() {

          beforeEach(function() {
            store = undefined;
          });

          it('should initialise the store when not initialised', inject(function(RdfstoreService) {

            spyOn(rdfstore, 'create');
            RdfstoreService.load(store, 'data');
            expect(rdfstore.create).toHaveBeenCalled();
          }));
        });

        describe('when initialised', function() {
          it('should not create a new store', inject(function(RdfstoreService) {
            spyOn(rdfstore, 'create');
            RdfstoreService.load(store, 'data');
            expect(rdfstore.create).not.toHaveBeenCalled();
          }));
        });

        it('should load from the store', inject(function($rootScope, RdfstoreService) {
          var result;
          mockStore.load = function(type, data, callback) {
            mockStore.data = 'mockData';
            callback();
          };

          spyOn(mockStore, 'load').and.callThrough();

          RdfstoreService.load(store, 'mockData').promise.then(function(filledStore) {
            result = filledStore;
          });

          $rootScope.$digest();

          expect(result).toEqual(mockStore);
          expect(mockStore.load).toHaveBeenCalledWith('application/ld+json', 'mockData', jasmine.any(Function));
        }));
      });

      describe('execute', function() {
        beforeEach(function() {
          store = mockStore;
        });

        describe('when not initialised', function() {

          beforeEach(function() {
            store = undefined;
          });

          it('should initialise the store when not initialised', inject(function(RdfstoreService) {

            spyOn(rdfstore, 'create');
            RdfstoreService.execute(store, 'query');
            expect(rdfstore.create).toHaveBeenCalled();
          }));
        });

        describe('when initialised', function() {
          it('should not create a new store', inject(function(RdfstoreService) {
            spyOn(rdfstore, 'create');
            RdfstoreService.execute(store, 'query');
            expect(rdfstore.create).not.toHaveBeenCalled();
          }));
        });

        describe('when the query succeeds', function() {
          it('should return a list of datasets', inject(function($rootScope, RdfstoreService) {
            var mockDatasets = [{
              id: 1
            }, {
              id: 2
            }],
            executeResult
            mockStore.execute = function(query, callback) {
              callback(true, mockDatasets);
            };
            spyOn(mockStore, 'execute').and.callThrough();

            RdfstoreService.execute(store, 'query').promise.then(function(result) {
              executeResult = result;
            });

            $rootScope.$digest();

            expect(executeResult.length).toBe(2);
            expect(mockStore.execute).toHaveBeenCalledWith('query', jasmine.any(Function));
          }));
        });

      });

    });
  });
