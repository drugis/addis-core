function DatasetsPage(browser) {
  this.browser = browser;
}

var LOCATORS = {
  body: 'body',
  selectedOutcome: 'select option[selected="selected"]'
};

DatasetsPage.prototype = {
  waitForPageToLoad: function() {
    this.browser.waitForElementVisible(LOCATORS.body, 50000);
  },
  end: function() {
    this.browser.end();
  },
  checkSelectedOutcome: function(name) {
    this.browser
      .useXpath()
      .waitForElementVisible(LOCATORS.selectedOutcome, 15000)
      .assert.containsText(LOCATORS.selectedOutcome, name);
  }
};

module.exports = DatasetsPage;
