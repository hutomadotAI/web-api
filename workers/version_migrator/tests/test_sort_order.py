import migration_logic as logic


def entry_generator(number_entries):
    entries = [
        logic.RetrainBotEntry(ii, "aiid{}".format(ii), "devid{}".format(ii), 0)
        for ii in range(number_entries)
    ]
    return entries


def test_sort_reverse_id():
    entries = entry_generator(3)
    bots_sorted = sorted(entries)
    print(bots_sorted)
    assert bots_sorted[0].iid == 2
    assert bots_sorted[1].iid == 1
    assert bots_sorted[2].iid == 0


def test_sort_published_first_then_priority_then_id():
    entries = entry_generator(10)
    entries[5].is_published = True
    entries[6].is_published = True
    entries[8].is_priority = True
    entries[2].is_priority = True

    bots_sorted = sorted(entries)
    print(bots_sorted)
    assert bots_sorted[0].iid == 6
    assert bots_sorted[1].iid == 5
    assert bots_sorted[2].iid == 8
    assert bots_sorted[3].iid == 2
    assert bots_sorted[4].iid == 9
