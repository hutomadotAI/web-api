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