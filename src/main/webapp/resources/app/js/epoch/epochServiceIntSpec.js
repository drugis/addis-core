'use strict';
define(['angular', 'angular-mocks'], function() {
  fdescribe('the epoch service', function() {

    var rootScope, q, epochService,
      queryEpochs, queryAddEpoch, queryAddEpochToEndOfList, queryAddEpochComment,
      editEpoch, addEpochToEndOfList, setEpochPrimary, deleteEpoch,
      removeEpochPrimary, setEpochToPrimary,
      graphAsText, getLastItemInEpochList, deleteTail, nonModifyingqueryPromise,
      studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);
    var studyDefer;
    var studyJsonObject;

    beforeEach(module('trialverse.epoch'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, EpochService) {

      q = $q;
      rootScope = $rootScope;
      epochService = EpochService;
      studyDefer = $q.defer();
      var getStudyPromise = studyDefer.promise;

      studyJsonObject = {
        'has_epochs': [{
          '@id': 'http://trials.drugis.org/instances/aaa',
          '@type': 'ontology:Epoch',
          'duration': 'P14D',
          'label': 'Washout'
        }, {
          '@id': 'http://trials.drugis.org/instances/bbb',
          '@type': 'ontology:Epoch',
          'label': 'Randomization'
        }, {
          '@id': 'http://trials.drugis.org/instances/ccc',
          '@type': 'ontology:Epoch',
          'duration': 'P42D',
          'label': 'Main phase'
        }],
        'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
      };



      studyService.getStudy.and.returnValue(getStudyPromise);

    }));


    describe('queryItems', function(done) {

      var expected = [{
        '@id': 'http://trials.drugis.org/instances/aaa',
        '@type': 'ontology:Epoch',
        'duration': 'P14D',
        'label': 'Washout',
        'pos': 0,
        'isPrimary': false
      }, {
        '@id': 'http://trials.drugis.org/instances/bbb',
        '@type': 'ontology:Epoch',
        'label': 'Randomization',
        'pos': 1,
        'isPrimary': false
      }, {
        '@id': 'http://trials.drugis.org/instances/ccc',
        '@type': 'ontology:Epoch',
        'duration': 'P42D',
        'label': 'Main phase',
        'pos': 2,
        'isPrimary': false
      }];

      it('should query the epochs', function(done) {
        studyDefer.resolve(studyJsonObject);
        epochService.queryItems().then(function(result) {
          expect(result).toEqual(expected);
          done();
        });

        rootScope.$digest();

      });
    });

    describe('queryItems where primary is set', function(done) {

      var expected = [{
        '@id': 'http://trials.drugis.org/instances/aaa',
        '@type': 'ontology:Epoch',
        'duration': 'P14D',
        'label': 'Washout',
        'pos': 0,
        'isPrimary': false
      }, {
        '@id': 'http://trials.drugis.org/instances/ddd',
        '@type': 'ontology:Epoch',
        'label': 'Randomization',
        'pos': 1,
        'isPrimary': true
      }, {
        '@id': 'http://trials.drugis.org/instances/ccc',
        '@type': 'ontology:Epoch',
        'duration': 'P42D',
        'label': 'Main phase',
        'pos': 2,
        'isPrimary': false
      }];

      it('should query the epochs', function(done) {
        var getStudyPromise = studyDefer.promise;
        var studyJsonObject = {
          'has_epochs': [{
            '@id': 'http://trials.drugis.org/instances/aaa',
            '@type': 'ontology:Epoch',
            'duration': 'P14D',
            'label': 'Washout'
          }, {
            '@id': 'http://trials.drugis.org/instances/ddd',
            '@type': 'ontology:Epoch',
            'label': 'Randomization'
          }, {
            '@id': 'http://trials.drugis.org/instances/ccc',
            '@type': 'ontology:Epoch',
            'duration': 'P42D',
            'label': 'Main phase'
          }],
          'has_primary_epoch': 'http://trials.drugis.org/instances/ddd'
        };
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);
        epochService.queryItems().then(function(result) {
          expect(result).toEqual(expected);
          done();
        });

        rootScope.$digest();

      });
    });

    // describe('addEpoch', function() {

    //   it('should add the epoch with comment and set primary and add it to the list', function() {
    //     var mockEpoch = {
    //       label: 'new epoch label',
    //       comment: 'new epoch comment',
    //       isPrimaryEpoch: true,
    //       duration: {
    //         numberOfPeriods: 13,
    //         periodType: {
    //           value: 'D',
    //           type: 'day',
    //           label: 'day(s)'
    //         }
    //       }
    //     };
    //     studyService.doModifyingQuery.calls.reset();
    //     epochService.addItem(mockEpoch);

    //     rootScope.$digest();

    //     expect(studyService.doModifyingQuery.calls.count()).toEqual(4);

    //   });

    //   it('should add the epoch without comment', function() {
    //     var mockEpoch = {
    //       label: 'new epoch label',
    //       duration: {
    //         numberOfPeriods: 13,
    //         periodType: {
    //           value: 'D',
    //           type: 'day',
    //           label: 'day(s)'
    //         }
    //       }
    //     };
    //     studyService.doModifyingQuery.calls.reset();

    //     epochService.addItem(mockEpoch);

    //     rootScope.$digest();

    //     expect(studyService.doModifyingQuery.calls.count()).toEqual(2);

    //   });


    // });


    // describe('editEpoch', function() {
    //   it('should modify the epoch without comment', function() {
    //     var mockEpoch = {
    //       uri: {
    //         value: 'uri'
    //       },
    //       measurementType: {
    //         value: 'measType'
    //       },
    //       label: 'new epoch label',
    //       isPrimary: {
    //         value: 'true'
    //       },
    //       duration: {
    //         numberOfPeriods: 13,
    //         periodType: {
    //           value: 'D',
    //           type: 'day',
    //           label: 'day(s)'
    //         }
    //       }
    //     };
    //     studyService.doModifyingQuery.calls.reset();

    //     epochService.editItem(mockEpoch, mockEpoch);

    //     rootScope.$digest();

    //     expect(studyService.doModifyingQuery.calls.count()).toEqual(2);

    //   });
    // });

  });
});