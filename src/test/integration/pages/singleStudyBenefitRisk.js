function SingleStudyBenefitRiskPage(browser) {
  this.browser = browser;
}

var LOCATORS = {
  body: 'body',
  outcomeSelect: '#outcome-select'
};

SingleStudyBenefitRiskPage.prototype = {
  waitForPageToLoad: function() {
    this.browser.waitForElementVisible(LOCATORS.body, 50000);
  },
  end: function() {
    this.browser.end();
  },
  selectOutcome: function(name) {
    this.browser
      .waitForElementVisible(LOCATORS.outcomeSelect, 15000)
      .click(LOCATORS.outcomeSelect)
      .setValue(LOCATORS.outcomeSelect, name);
  },
  checkInterventionOverlap: function() {
    this.browser
      .assert.containsText(LOCATORS.overlapWarning, expextedOverlapText)
      .assert.attributeEquals(LOCATORS.createModelBtn, 'disabled', 'true');

  }
};

module.exports = SingleStudyBenefitRiskPage;
