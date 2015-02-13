define(['angular', 'angular-mocks'], function () {
  describe('The anchor filter', function () {
    var anchorFilter;

    beforeEach(module('trialverse.util'));

    beforeEach(inject(function($filter) {
      anchorFilter = $filter('anchorFilter');
    }));

    it('should pass though undefined durations and anchors', function() {
      expect(anchorFilter(undefined)).toEqual(undefined);
      expect(anchorFilter(undefined, 'PT4H')).toEqual(undefined);
      expect(anchorFilter('http://trials.drugis.org/ontology#anchorEpochStart',undefined)).toEqual(undefined);
      expect(anchorFilter(undefined,undefined)).toEqual(undefined);
    });

    it('should use "at" as preposition when the off set is instantaneous', function() {
      expect(anchorFilter('http://trials.drugis.org/ontology#anchorEpochStart', 'PT0S')).toEqual(' at epoch start');
      expect(anchorFilter('http://trials.drugis.org/ontology#anchorEpochEnd', 'PT0S')).toEqual(' at epoch end');
    }); 

    it('should use "from" as preposition when the off set is instantaneous', function() {
      expect(anchorFilter('http://trials.drugis.org/ontology#anchorEpochStart', 'PT4H')).toEqual(' from epoch start');
      expect(anchorFilter('http://trials.drugis.org/ontology#anchorEpochEnd', 'PT4H')).toEqual(' from epoch end');
    }); 

  });
});
