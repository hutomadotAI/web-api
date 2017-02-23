import matplotlib.pyplot as plt

import hu_api
from performance.load_test import load_test
from performance.performance_config import Config

plt.close('all')

config = Config()
requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])
requesterLoad = hu_api.api.ApiRequester(config.url_root, config.chat_auth, [])

results = load_test(config, requester, requesterLoad)
results.sort(key=lambda tup: tup[1])
simultaneous_range = set([x for (x, y, z) in results])

fig, ax = plt.subplots()
for simultaneous in simultaneous_range:
    generated_x = [(x) for (a, x, y) in results if a == simultaneous]
    generated_y = [(y) for (a, x, y) in results if a == simultaneous]
    ax.plot(generated_x, generated_y, label="{0} simultaneous".format(simultaneous))
    ax.scatter(generated_x, generated_y)

plt.ylabel("Duration")
plt.xlabel("AI Size in lines")
ax.legend()
plt.show()

exit(0)
