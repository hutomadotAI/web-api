/*
 Test to see if existing item is found correctly.
 */
QUnit.test("IsNameExists finds entry", function (assert) {
    var exists = validation.isNameExists("name", ["notname","name"]);
    assert.ok(exists == true, "Passed");
});

/*
 Test that non-existent item isn't found.
 */
QUnit.test("IsNameExists does not find entry", function (assert) {
    var exists = validation.isNameExists("names", ["notname","name"]);
    assert.ok(exists == false, "Passed");
});


/** Test that URI item is allowed. **/

QUnit.test("isInputInvalid allows entry", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("isInputInvalid allows entry", function (assert) {
    var allowed = validation.isInputInvalid('http://hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("isInputInvalid allows entry", function (assert) {
    var allowed = validation.isInputInvalid('www.hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("isInputInvalid allows entry", function (assert) {
    var allowed = validation.isInputInvalid('hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("isInputInvalid allows entry", function (assert) {
    var allowed = validation.isInputInvalid('https://www.hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("isInputInvalid allows entry", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com/pages','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("isInputInvalid allows entry", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com/pages/login.php','URI');
    assert.ok(allowed == false, "Passed");
});



/** Test that URI item is denied. **/

QUnit.test("isInputInvalid denied entry", function (assert) {
    var allowed = validation.isInputInvalid('','URI');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("isInputInvalid denied entry", function (assert) {
    var allowed = validation.isInputInvalid(' ','URI');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("isInputInvalid denied entry", function (assert) {
    var allowed = validation.isInputInvalid('hutoma','URI');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("isInputInvalid denied entry", function (assert) {
    var allowed = validation.isInputInvalid('www.hutoma','URI');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("isInputInvalid denied entry", function (assert) {
    var allowed = validation.isInputInvalid('abc://www.hutoma.com','URI');
    assert.ok(allowed == true, "Passed");
});