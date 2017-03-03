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

QUnit.test("URL with http and www is valid", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("URL with http and without www is valid", function (assert) {
    var allowed = validation.isInputInvalid('http://hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("URL without http is valid", function (assert) {
    var allowed = validation.isInputInvalid('www.hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("URL without http and www is valid", function (assert) {
    var allowed = validation.isInputInvalid('hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("URL with http protocol is valid", function (assert) {
    var allowed = validation.isInputInvalid('https://www.hutoma.com','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("URL with a path is valid", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com/pages','URI');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("URL with a path to file is valid", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com/pages/login.php','URI');
    assert.ok(allowed == false, "Passed");
});



/** Test that URI item is denied. **/

QUnit.test("URL empty is not valid", function (assert) {
    var allowed = validation.isInputInvalid('','URI');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("URL composed by single word is not valid", function (assert) {
    var allowed = validation.isInputInvalid('hutoma','URI');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("URL without top-level domain is invalid", function (assert) {
    var allowed = validation.isInputInvalid('www.hutoma','URI');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("URL with different protocol by http and https is not valid", function (assert) {
    var allowed = validation.isInputInvalid('abc://www.hutoma.com','URI');
    assert.ok(allowed == true, "Passed");
});